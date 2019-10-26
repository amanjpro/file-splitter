package me.amanj.file.splitter

import org.scalatest._
import java.io.{File, PrintWriter}
import scala.io.Source
import java.nio.file.{Files, Path}

class AppSpecIt extends FlatSpec with
  Matchers with BeforeAndAfterEach {

  var in: Path = _
  var out: Path = _
  var outDir: Path = _

  override def afterEach(): Unit = {
    new File(in.toString).delete
    new File(out.toString).delete
    new File(outDir.toString).delete
    super.afterEach
  }

  override def beforeEach(): Unit = {
    in = Files.createTempFile("test", "input")
    out = Files.createTempFile("test", "out")
    outDir = Files.createTempDirectory("testdir")

    val writer = new PrintWriter(in.toString)
    writer.println(1)
    writer.println(2)
    writer.println(3)
    writer.println(4)
    writer.close

    super.beforeEach
  }

  "App" should "respect -n option" in {
    val args = Array (
      "-i", s"file://${in.toString}",
      "-o", s"file://${outDir.toString}",
      "-n", "4"
    )

    // run the application
    App.main(args)

    val outputFiles = new File(outDir.toString).listFiles
    outputFiles.length shouldBe 4
    outputFiles.foreach { file =>
      file.length shouldBe 2L
    }
  }

  it should "respect --keep-order option" in {
    val args = Array (
      "-i", s"file://${in.toString}",
      "-o", s"file://${outDir.toString}",
      "-n", "2",
      "--keep-order"
    )

    // run the application
    App.main(args)

    val firstLines =
      Source.fromFile(s"${outDir.toString}/part-00000").getLines
    val secondLines =
      Source.fromFile(s"${outDir.toString}/part-00001").getLines

    firstLines.toList shouldBe List("1", "2")
    secondLines.toList shouldBe List("3", "4")
  }
}

