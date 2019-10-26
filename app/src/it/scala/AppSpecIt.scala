package me.amanj.file.splitter

import me.amanj.file.splitter.compression.Gzip
import java.io.{File, PrintWriter, FileInputStream}
import scala.io.Source
import java.nio.file.{Files, Path}
import java.security.Permission

import org.scalatest._

class AppSpecIt extends FlatSpec with
  Matchers with BeforeAndAfterEach {

  var in: Path = _
  var out: Path = _
  var outDir: Path = _

  override def afterEach(): Unit = {
    new File(in.toString).delete
    new File(out.toString).delete
    new File(outDir.toString).delete
    System.setSecurityManager(null)
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

  it should "understand -x option" in {
    val args = Array (
      "-i", s"file://${getClass.getResource("/test.gz").getFile}",
      "-o", s"file://${outDir.toString}",
      "-x", "gzip",
    )

    // run the application
    App.main(args)

    val lines =
      Source.fromFile(s"${outDir.toString}/part-00000").getLines

    lines.toList shouldBe List("1", "2", "3", "4")
  }

  it should "understand -z option" in {
    val args = Array (
      "-i", s"file://${getClass.getResource("/test").getFile}",
      "-o", s"file://${outDir.toString}",
      "-z", "gzip",
    )

    // run the application
    App.main(args)

    val reader = Gzip.reader(
      new FileInputStream(s"${outDir.toString}/part-00000.gz"))
    var actual = ""
    var next = reader.read
    while(next != -1) {
      actual += next.toChar
      next = reader.read
    }

    actual shouldBe "1\n2\n3\n4\n"
  }

  sealed case class ExitException(status: Int)
    extends SecurityException("System.exit() is not allowed")

  sealed class NoExitSecurityManager extends SecurityManager {
    override def checkPermission(perm: Permission): Unit = {}
    override def checkPermission(perm: Permission,
      context: Object): Unit = {}

    override def checkExit(status: Int): Unit = {
      super.checkExit(status)
      throw ExitException(status)
    }
  }

  it should "fail if input does not exist" in {
    val args = Array (
      "-i", s"file://${outDir.toString}/nope}",
      "-o", s"file://${outDir.toString}",
    )

    System.setSecurityManager(new NoExitSecurityManager())
    try {
      // run the application
      App.main(args)
    } catch {
      case e: ExitException =>
        e.status shouldBe 1
    }
  }

  it should "fail if output exists" in {
    val args = Array (
      "-i", s"file://${in.toString}",
      "-9", s"file://${in.toString}",
    )

    System.setSecurityManager(new NoExitSecurityManager())
    try {
      // run the application
      App.main(args)
    } catch {
      case e: ExitException =>
        e.status shouldBe 1
    }
  }
}

