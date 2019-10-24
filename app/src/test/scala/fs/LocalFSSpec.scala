package me.amanj.file.splitter.fs

import org.scalatest._
import java.io.{PrintWriter, BufferedReader, InputStreamReader,
  File}
import java.nio.file.{Path, Files}

// Java interop
import scala.jdk.CollectionConverters._

class LocalFSSpec extends FlatSpec
  with Matchers with BeforeAndAfterEach {

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
  "separator" should "be /" in {
    LocalFS.separator shouldBe "/"
  }

  "extractFilePath" should "remove leading file:// from path" in {
    LocalFS.extractFilePath("file://file_name") shouldBe "file_name"
  }

  it should "remove only the first file:// from path" in {
    LocalFS.extractFilePath("file://file://file_name") shouldBe "file://file_name"
  }

  it should "not remove non-leading file:// from path" in {
    LocalFS.extractFilePath("yay://file://file_name") shouldBe "yay://file://file_name"
  }

  "exists" should "return true if file exists" in {
    LocalFS.exists(System.getProperty("user.home")) shouldBe true
  }

  it should "return false if file exists" in {
    LocalFS.exists("/malmo") shouldBe false
  }

  "source" should "should get input stream of path" in {
   val lines = new BufferedReader(
      new InputStreamReader(
        LocalFS.source(in.toString)
      )
    ).lines.iterator.asScala.toList
    lines shouldBe List("1")
  }

  "sink" should "should get output stream of path" in {
    val printer = new PrintWriter(
      LocalFS.sink(out.toString))
    printer.print("2")
    printer.close

    val lines = new BufferedReader(
      new InputStreamReader(
        LocalFS.source(out.toString)
      )
    ).lines.iterator.asScala.toList
    lines shouldBe List("2")
  }
}

