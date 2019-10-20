package me.amanj.file.splitter.fs

import org.scalatest._
import software.amazon.awssdk.regions.Region

class S3Spec extends FlatSpec with Matchers {

  var s3 = S3(Region.of("us-east-1"))

  "separator" should "be /" in {
    s3.separator shouldBe "/"
  }

  "extractFilePath" should "not remove leading s3:// from path" in {
    s3.extractFilePath("s3://file_name") shouldBe "s3://file_name"
  }

  "bucket" should "return the bucket part of s3://my_b/my_p/kk" in {
    s3.bucket("s3://my_b/my_p/kk") shouldBe "my_b"
  }

  "bucket" should "return the bucket part of s3://my_b" in {
    s3.bucket("s3://my_b") shouldBe "my_b"
  }

  "key" should "return nothing from s3://my_b" in {
    s3.key("s3://my_b") shouldBe ""
  }
}
