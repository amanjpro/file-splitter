package me.amanj.file.splitter

import org.scalatest._

class AppSpec extends FlatSpec with Matchers {
  "getPartNames" should "generate correct file parts" in {
    val expected = Seq(
      "/tmp/part-00000.gz", "/tmp/part-00001.gz", "/tmp/part-00002.gz")
    val actual = App.getPartNames("/tmp", "/", 3, ".gz")

    actual should contain theSameElementsAs expected
  }
}

