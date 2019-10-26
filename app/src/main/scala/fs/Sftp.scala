package me.amanj.file.splitter.fs

// SSH related imports
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.OpenMode
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import net.schmizz.sshj.common.{KeyType, SecurityUtils}
import java.security.PublicKey

// IO and Misc
import java.util.EnumSet
import java.io.{InputStream, OutputStream, File, IOException}
import scala.io.StdIn

case class Sftp(auth: Sftp.Auth,
    knownHosts: String = Sftp.KnownHosts,
    publicKeyLocations: Array[String] = Sftp.PublicKeyLocations) extends FS {
  private val scheme = "sftp://"
  private val host = "[^/:]+"
  private val port = "\\d+"
  private val file = ".*"
  private val SftpRegex = s"$scheme($host)(:$port)?/($file)".r
  def host(path: String): String = path match {
    case SftpRegex(host, _, _) => host
  }

  def port(path: String): Int = path match {
    case SftpRegex(_, null, _) => 22
    case SftpRegex(_, port, _) => port.tail.toInt
  }

  def remoteFile(path: String): String = path match {
    case SftpRegex(_, _, file) => file
  }

  def getStream[T](host: String, port: Int)(get: SSHClient => T): T = {
    val ssh = new SSHClient
    ssh.loadKnownHosts(new File(knownHosts))
    val hostVerifier =
      new Sftp.OpenSSHKnownHostsInteractive(new File(knownHosts))
    ssh.addHostKeyVerifier(hostVerifier)
    ssh.connect(host, port)
    auth match {
      case Sftp.KeyAuth(username) =>
        ssh.authPublickey(username, publicKeyLocations: _*)
      case Sftp.Login(username, password) =>
        ssh.authPassword(username, password)
    }
    get(ssh)
  }

  def source(path: String): InputStream =
    getStream(host(path), port(path)) { case ssh =>
      val client = ssh.newSFTPClient
      val handle = client.open(remoteFile(path), EnumSet.of(OpenMode.READ))
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
        client.open(remoteFile(path),
          EnumSet.of(OpenMode.WRITE, OpenMode.CREAT))
      } catch {
        case e: IOException =>
          println(e.getMessage)
          ???
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
  def extractFilePath(path: String): String = path

  def exists(path: String): Boolean = getStream(host(path), port(path)) {
    case ssh => {
      val client = ssh.newSFTPClient
      try {
        client.open(remoteFile(path), EnumSet.of(OpenMode.READ))
        client.close
        ssh.disconnect
        true
      } catch {
        case _: IOException =>
          client.close
          ssh.disconnect
          false
      }
    }
  }

  def size(path: String): Long = getStream(host(path), port(path)) {
    case ssh => {
      val client = ssh.newSFTPClient
      val handle = client.open(remoteFile(path), EnumSet.of(OpenMode.READ))
      val size = handle.length
      client.close
      ssh.disconnect
      size
    }
  }
}

object Sftp {
  val DefaultPublicKeyLocation =
    s"${System.getProperty("user.home")}/.ssh/id_rsa"
  val DefaultKnownHosts =
    s"${System.getProperty("user.home")}/.ssh/known_hosts"

  val KnownHosts = System.getenv.getOrDefault("KNOWN_HOSTS",
    Sftp.DefaultKnownHosts)
  val PublicKeyLocations = System.getenv
    .getOrDefault(
      "PUBLIC_KEY_LOCATIONS", Sftp.DefaultPublicKeyLocation)
    .split(',')

  sealed trait Auth
  case class Login(username: String, password: String) extends Auth
  case class KeyAuth(username: String) extends Auth

  class OpenSSHKnownHostsInteractive(file: File)
      extends OpenSSHKnownHosts(file) {
    override protected def hostKeyUnverifiableAction(hostname: String,
        key: PublicKey): Boolean = {
      val tpe = KeyType.fromKey(key)
      val msg =
        s"""|The authenticity of host '$hostname' can't be established
            |
            |$tpe key fingerprint is ${SecurityUtils.getFingerprint(key)}
            |
            |
            |Are you sure you want to continue connecting? [yes/no]\n"""
              .stripMargin
      val answer = StdIn.readLine(msg)
      answer.toLowerCase match {
        case "yes" =>
          entries.add(new OpenSSHKnownHosts.HostEntry(null, hostname,
            KeyType.fromKey(key), key))
          try {
            write();
            println(
              s"## Warning: Permanently added '$hostname' ($tpe) to the list of known hosts")
            true
          } catch {
            case ex: IOException =>
              println(ex);
              println(
                s"## Warning: Could not add '$hostname' ($tpe) to the list of known hosts.")
            true
          }
        case _ =>
          println(s"SHOOOT, answer was $answer")
          false
      }
    }

    override protected def hostKeyChangedAction(hostname: String,
        key: PublicKey): Boolean = {
      val tpe = KeyType.fromKey(key)
      val fp = SecurityUtils.getFingerprint(key)
      val path = getFile().getAbsolutePath()
      val msg =
        s"""|@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            |@    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!     @
            |@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            |IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!
            |Someone could be eavesdropping on you right now
            |(man-in-the-middle attack)!
            |
            |It is also possible that the host key has just been changed.
            |The fingerprint for the $tpe key sent by the remote host is
            |$fp.
            |
            |Please contact your system administrator or
            |add correct host key in $path to get rid of this message."""
              .stripMargin
      println(msg)
      false
    }
  }
}
