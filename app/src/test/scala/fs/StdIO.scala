package me.amanj.file.splitter.fs

import org.scalatest._
import java.io.{ByteArrayInputStream, BufferedReader,
  InputStreamReader, OutputStreamWriter, ByteArrayOutputStream,
  PrintWriter, PrintStream}

class StdIOFSSpec extends FlatSpec with Matchers {
  "separator" should "be empty" in {
    StdIO.separator shouldBe ""
  }

  "extractFilePath" should "return path verbatim" in {
    StdIO.extractFilePath("here") shouldBe "here"
  }

  "exists" should "return false for anything but stdin" in {
    StdIO.exists("malmo") shouldBe false
    StdIO.exists("stdout") shouldBe false
    StdIO.exists("stdout.gz") shouldBe false
  }

  it should "return true for stdin" in {
    StdIO.exists("stdin") shouldBe true
  }

  "source" should "read from stdin" in {
    val inputStr =
      """|First line
         |Second line
         |""".stripMargin
    // val in = new StringReader(inputStr)
    var lines = ""
    System.setIn(new ByteArrayInputStream(inputStr.getBytes()));

    new BufferedReader(new InputStreamReader(StdIO.source("")))
      .lines().forEach { line =>
        lines += s"$line\n"
      }
    lines shouldBe inputStr
  }

  "sink" should "write to stdout" in {
    val baos =new ByteArrayOutputStream
    val ps = new PrintStream(baos)
    System.setOut(ps)


    val printer = new PrintWriter(new OutputStreamWriter(StdIO.sink("")))
    printer.print("Yay")
    printer.close
    baos.toString shouldBe "Yay"
  }
}

