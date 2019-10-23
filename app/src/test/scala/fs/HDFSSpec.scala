package me.amanj.file.splitter.fs

import org.scalatest._

class HDFSSpec extends FlatSpec with Matchers {
  "separator" should "be /" in {
    HDFS().separator shouldBe "/"
  }

  "extractFilePath" should "remove leading hdfs:// from path" in {
    HDFS().extractFilePath("hdfs://file_name") shouldBe "file_name"
  }

  it should "remove only the first hdfs:// from path" in {
    HDFS().extractFilePath("hdfs://hdfs://file_name") shouldBe "hdfs://file_name"
  }

  it should "not remove non-leading hdfs:// from path" in {
    HDFS().extractFilePath("yay://hdfs://file_name") shouldBe "yay://hdfs://file_name"
  }
}

