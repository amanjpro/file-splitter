package me.amanj.file.splitter.fs

import org.scalatest._

import java.nio.file.{Files, Path}
import java.io.{File, PrintWriter, BufferedReader, InputStreamReader}
import org.apache.hadoop.fs.{Path => HPath}

// Java interop
import scala.collection.JavaConverters._
import org.apache.hadoop.fs.FileSystem

class HDFSSpecIt extends FlatSpec with
  Matchers with BeforeAndAfterEach {

  val hdfs = new HDFS("http://localhost:9000")

  var in: Path = _
  var out: Path = _

  override def afterEach(): Unit = {
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
    hdfs.exists("/foo/bar/baz") shouldBe false
  }

  "exists" should "return true when object is found" in {
    hdfs.exists("/") shouldBe true
  }

  "size" should "return size of the object" in {
    hdfs.fileSystem.copyFromLocalFile(new HPath(in.toString),
      new HPath("/test.txt"))
    hdfs.size("/test.txt") shouldBe 1
  }

  "source" should "should get input stream of path" in {
    hdfs.fileSystem.copyFromLocalFile(new HPath(in.toString),
      new HPath("/test.txt"))

    val lines = new BufferedReader(
      new InputStreamReader(
        hdfs.source("/test.txt")
      )
    ).lines.iterator.asScala.toList
    lines shouldBe List("1")
  }

  "sink" should "should get output stream of path" in {
    val printer = new PrintWriter(
      hdfs.sink("/test"))
    printer.print("2")
    printer.close

    val lines = new BufferedReader(
      new InputStreamReader(
        hdfs.source("/test.txt")
      )
    ).lines.iterator.asScala.toList
    lines shouldBe List("2")
  }
}

