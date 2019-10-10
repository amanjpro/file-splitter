package me.amanj.file.splitter.fs

import org.scalatest._

class HDFSSpec extends FlatSpec with Matchers {
  "separator" should "be /" in {
    new HDFS().separator shouldBe "/"
  }

  "extractFilePath" should "remove leading hdfs:// from path" in {
    new HDFS().extractFilePath("hdfs://file_name") shouldBe "file_name"
  }

  "extractFilePath" should "remove only the first hdfs:// from path" in {
    new HDFS().extractFilePath("hdfs://hdfs://file_name") shouldBe "hdfs://file_name"
  }

  "extractFilePath" should "not remove non-leading hdfs:// from path" in {
    new HDFS().extractFilePath("yay://hdfs://file_name") shouldBe "yay://hdfs://file_name"
  }
}

