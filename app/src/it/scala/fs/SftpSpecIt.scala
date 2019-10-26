package me.amanj.file.splitter.fs

import org.scalatest._

import java.nio.file.{Files, Path}
import java.io.{File, PrintWriter, BufferedReader, InputStreamReader, StringReader}
// import java.io.{File, PrintWriter, StringReader} //, BufferedReader, InputStreamReader}

// Java interop
import scala.jdk.CollectionConverters._

class SftpSpecIt extends FlatSpec with
  Matchers with BeforeAndAfterEach {
  var in: Path = _
  var out: Path = _

  val knownHosts = "" / "tmp" / "ssh_known_hosts"
  val sftp = new Sftp(Sftp.Login("bar", "baz"), knownHosts)

  implicit class StrExt(parent: String) {
    def /(child: String): String =
      s"${parent}${File.separator}$child"
  }

  def yes[T](action: => T): T = {
    Console.withIn(new StringReader("yes\n")) {
      action
    }
  }

  def no[T](action: => T): T = {
    Console.withIn(new StringReader("no\n")) {
      action
    }
  }

  override def afterEach(): Unit = {
    new File(in.toString).delete
    new File(out.toString).delete
    new File(knownHosts).delete
    super.afterEach
  }

  override def beforeEach(): Unit = {
    in = Files.createTempFile("test", "input")
    out = Files.createTempFile("test", "out")

    val writer = new PrintWriter(in.toString)
    writer.print(1)
    writer.close

    super.beforeEach
  }

  "exists" should "return false when object is not found" in {
    val exists = yes {
      sftp.exists("sftp://localhost:2222/upload/nope")
    }
    exists shouldBe false
  }

  it should "return true when object is found" in {
    val exists = yes {
      sftp.exists("sftp://localhost:2222/upload/test")
    }
    exists shouldBe true
  }

  "size" should "return size of the object" in {
    val size = yes {
      sftp.size("sftp://localhost:2222/upload/test")
    }
    size shouldBe 1
  }


  "source" should "should get input stream of path" in {
    val lines = yes {
      new BufferedReader(
        new InputStreamReader(
          sftp.source("sftp://localhost:2222/upload/test")
        )
      ).lines.iterator.asScala.toList
    }

    lines shouldBe List("1")
  }

  "sink" should "should get output stream of path" in {
    yes {
      val printer = new PrintWriter(
        sftp.sink("sftp://localhost:2222/upload/test"))
      printer.print("2")
      printer.close
    }

    val lines = new BufferedReader(
      new InputStreamReader(
        sftp.source("sftp://localhost:2222/upload/test")
      )
    ).lines.iterator.asScala.toList

    lines shouldBe List("2")
  }
}

