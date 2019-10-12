package me.amanj.file.splitter.fs

import java.io.{InputStream, OutputStream, File, IOException}
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.{SFTPClient, OpenMode}
import net.schmizz.sshj.xfer.FileSystemFile
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.common.DisconnectReason
import java.util.EnumSet
import scala.io.StdIn


class Sftp(username: String, password: String) {
  private val scheme = "sftp://"
  private val host = "[^/:]+"
  private val port = "\\d+"
  private val file = ".*"
  private val SftpRegex = s"$scheme($host)(:$port)?/($file)".r
  private val KnownHosts = System.getenv.getOrDefault("KNOWN_HOSTS",
    s"${System.getProperty("user.home")}/.ssh/known_hosts")

  def host(path: String): String = path match {
    case SftpRegex(host, _, _) => host
  }

  def port(path: String): Int = path match {
    case SftpRegex(_, null, _) => 22
    case SftpRegex(_, port, _) => port.tail.toInt
  }

  def getStream[T](host: String, port: Int)(get: SSHClient => T): T = {
    var ssh = new SSHClient
    ssh.loadKnownHosts(new File(KnownHosts))
    try {
      ssh.connect(host, port)
    } catch {
      case e: TransportException =>
        if (e.getDisconnectReason ==
          DisconnectReason.HOST_KEY_NOT_VERIFIABLE) {
          val msg = e.getMessage()
          val answer =
            StdIn.readLine(s"$msg\nConnect anyways? [yes/no].\n")
          answer.toLowerCase match {
            case "yes" =>
              val split = msg.split("`")
              val vc = split(3)
              ssh = new SSHClient();
              ssh.addHostKeyVerifier(vc)
              ssh.connect(host, port)
            case _     => System.exit(0)
          }
        } else throw e
    }
    ssh.authPassword(username, password)
    get(ssh)
  }

  def source(path: String): InputStream =
    getStream(host(path), port(path)) { case ssh =>
      val client = ssh.newSFTPClient
      val handle = client.open(extractFilePath(path), EnumSet.of(OpenMode.READ))
      new handle.ReadAheadRemoteFileInputStream(16) {
        override def close(): Unit = {
          super.close();
          handle.close();
          client.close
          ssh.disconnect
        }
      }
    }

  def sink(path: String): OutputStream =
    getStream(host(path), port(path)) { case ssh =>
      val client = ssh.newSFTPClient
      val handle = try {
        client.open(extractFilePath(path), EnumSet.of(OpenMode.WRITE))
      } catch {
        case _: IOException =>
          client.open(extractFilePath(path), EnumSet.of(OpenMode.CREAT))
      }
      new handle.RemoteFileOutputStream() {
          override def close(): Unit = {
            super.close();
            handle.close();
            client.close
            ssh.disconnect
          }
        }
    }
  def separator: String = "/"
  def extractFilePath(path: String): String = path match {
    case SftpRegex(_, _, file) => file
  }

  def size(path: String): Long = getStream(host(path), port(path)) {
    case ssh => {
      val client = ssh.newSFTPClient
      val handle = client.open(extractFilePath(path), EnumSet.of(OpenMode.READ))
      val size = handle.length
      client.close
      ssh.disconnect
      size
    }
  }
}
