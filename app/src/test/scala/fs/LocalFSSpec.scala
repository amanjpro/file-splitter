package me.amanj.file.splitter.fs

import org.scalatest._

class LocalFSSpec extends FlatSpec with Matchers {
  "separator" should "be /" in {
    LocalFS.separator shouldBe "/"
  }

  "extractFilePath" should "remove leading file:// from path" in {
    LocalFS.extractFilePath("file://file_name") shouldBe "file_name"
  }

  "extractFilePath" should "remove only the first file:// from path" in {
    LocalFS.extractFilePath("file://file://file_name") shouldBe "file://file_name"
  }

  "extractFilePath" should "not remove non-leading file:// from path" in {
    LocalFS.extractFilePath("yay://file://file_name") shouldBe "yay://file://file_name"
  }
}

