package me.amanj.file.splitter

import org.scalatest._

class AppSpec extends FlatSpec with Matchers {
  "getPartNames" should "generate correct file parts" in {
    App.getPartNames("/tmp", "/", 3) should contain theSameElementsAs Seq(
      "/tmp/part-00000", "/tmp/part-00001", "/tmp/part-00002")
  }
}

