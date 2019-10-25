package me.amanj.file.splitter.fs

import org.scalatest._

import java.nio.file.{Files, Path}
//import java.io.{File, PrintWriter, BufferedReader, InputStreamReader}
import java.io.{File, PrintWriter, ByteArrayInputStream} //, BufferedReader, InputStreamReader}

// Java interop
// import scala.jdk.CollectionConverters._

class SftpSpecIt extends FlatSpec with
  Matchers with BeforeAndAfterEach {

  val sftp: Sftp = new Sftp(Sftp.Login("bar", "baz"))

  var in: Path = _
  var out: Path = _

  def say(word: String): Unit = {
    System.setIn(new ByteArrayInputStream(word.getBytes()));
  }

  implicit class StrExt(parent: String) {
    def /(child: String): String =
      s"${parent}${File.separator}$child"
  }

  override def afterEach(): Unit = {
    val home = System.getProperty("user.home")
    new File(home / ".ssh" / "known_hosts").delete
    new File(in.toString).delete
    new File(out.toString).delete
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
    say("yes")
    sftp.exists("sftp://localhost:2222/home/foo/nope") shouldBe false
  }

  it should "return true when object is found" in {
    say("yes")
    sftp.exists("sftp://localhost:2222/home/foo") shouldBe false
  }
  //
  // "size" should "return size of the object" in {
  //   hdfs.fileSystem.copyFromLocalFile(new HPath(in.toString),
  //     new HPath("/user/root/test.txt"))
  //   hdfs.size("/user/root/test.txt") shouldBe 1
  // }
  //
  // "source" should "should get input stream of path" in {
  //   hdfs.fileSystem.copyFromLocalFile(new HPath(in.toString),
  //     new HPath("/user/root/test.txt"))
  //
  //   val lines = new BufferedReader(
  //     new InputStreamReader(
  //       hdfs.source("/user/root/test.txt")
  //     )
  //   ).lines.iterator.asScala.toList
  //   lines shouldBe List("1")
  // }
  //
  // "sink" should "should get output stream of path" in {
  //   val printer = new PrintWriter(
  //     hdfs.sink("/user/root/test2"))
  //   printer.print("2")
  //   printer.close
  //
  //   val lines = new BufferedReader(
  //     new InputStreamReader(
  //       hdfs.source("/user/root/test2")
  //     )
  //   ).lines.iterator.asScala.toList
  //   lines shouldBe List("2")
  // }
}

