package me.amanj.file.splitter.fs

import org.scalatest._

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
}

