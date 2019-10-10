package me.amanj.file.splitter.fs

import software.amazon.awssdk.regions.Region
import org.scalatest._

class S3Spec extends FlatSpec with Matchers {
  "separator" should "be /" in {
    new S3(Region.of("us-east-1")).separator shouldBe "/"
  }

  "extractFilePath" should "not remove leading s3:// from path" in {
    new S3(Region.of("us-east-1")).extractFilePath("s3://file_name") shouldBe "s3://file_name"
  }

  "bucket" should "return the bucket part of s3://my_b/my_p/kk" in {
    new S3(Region.of("us-east-1")).bucket("s3://my_b/my_p/kk") shouldBe "my_b"
  }

  "bucket" should "return the bucket part of s3://my_b" in {
    new S3(Region.of("us-east-1")).bucket("s3://my_b") shouldBe "my_b"
  }

  "key" should "return nothing from s3://my_b" in {
    new S3(Region.of("us-east-1")).key("s3://my_b") shouldBe ""
  }

  "key" should "return the key part of s3://my_b/my_k/p" in {
    new S3(Region.of("us-east-1")).key("s3://my_b/my_k/p") shouldBe "my_k/p"
  }
}

