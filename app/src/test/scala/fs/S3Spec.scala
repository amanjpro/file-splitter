package me.amanj.file.splitter.fs

import org.scalatest._
import io.findify.s3mock.S3Mock

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.http.SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES
import software.amazon.awssdk.utils.AttributeMap
import software.amazon.awssdk.services.s3.model.BucketCannedACL
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest
import java.io.{File, PrintWriter, BufferedReader, InputStreamReader}
import java.lang.Boolean.TRUE
import java.nio.file.Files
import java.nio.file.{Files, Path}
import java.net.URI
import scala.collection.JavaConverters._



class S3Spec extends FlatSpec with
  Matchers with BeforeAndAfterEach with BeforeAndAfterAll {

  var api: S3Mock = _
  var s3Client: S3Client = _
  val credential = AwsBasicCredentials.create("key", "secret")

  var in: Path = _
  var out: Path = _

  override def afterAll(): Unit = {
    api.shutdown
    super.afterAll
  }

  override def beforeAll(): Unit = {
    api = S3Mock(port = 8002, dir = "/tmp/s3")
    api.start

    s3Client =
      S3Client
        .builder
        .region(Region.of("us-east-1"))
        .endpointOverride(URI.create("http://localhost:8002"))
        .credentialsProvider(
          StaticCredentialsProvider.create(credential))
        .httpClient(UrlConnectionHttpClient.builder()
          .buildWithDefaults(AttributeMap.builder()
            .put(TRUST_ALL_CERTIFICATES, TRUE)
          .build()))
        .build

    super.beforeAll
  }

  override def afterEach(): Unit = {
    s3Client.deleteBucket(DeleteBucketRequest.builder.bucket("foo").build)
    new File(in.toString).delete
    new File(out.toString).delete
    super.afterEach
  }

  override def beforeEach(): Unit = {
    in = Files.createTempFile("test", "input")
    out = Files.createTempFile("test", "out")

    val writer = new PrintWriter(in.toString)
    writer.print(1)
    writer.close

    s3Client.createBucket(
      CreateBucketRequest.builder
      .bucket("foo").build)
    super.beforeEach
  }

  "separator" should "be /" in {
    new S3(s3Client).separator shouldBe "/"
  }

  "extractFilePath" should "not remove leading s3:// from path" in {
    new S3(s3Client).extractFilePath("s3://file_name") shouldBe "s3://file_name"
  }

  "bucket" should "return the bucket part of s3://my_b/my_p/kk" in {
    new S3(s3Client).bucket("s3://my_b/my_p/kk") shouldBe "my_b"
  }

  "bucket" should "return the bucket part of s3://my_b" in {
    new S3(s3Client).bucket("s3://my_b") shouldBe "my_b"
  }

  "key" should "return nothing from s3://my_b" in {
    new S3(s3Client).key("s3://my_b") shouldBe ""
  }

  "key" should "return the key part of s3://my_b/my_k/p" in {
    new S3(s3Client).key("s3://my_b/my_k/p") shouldBe "my_k/p"
  }

  "exists" should "return false when object is not found" in {
    new S3(s3Client).exists("s3://foo/bar/baz") shouldBe false
  }

  "exists" should "return true when object is found" in {
    s3Client.putObject(
      PutObjectRequest.builder
        .bucket("foo")
        .key("bar/baz").build, in)

    new S3(s3Client).exists("s3://foo/bar/baz") shouldBe true
  }

  "size" should "return size of the object" in {
    s3Client.putObject(
      PutObjectRequest.builder
        .bucket("foo")
        .key("bar/baz").build, in)
    new S3(s3Client).size("s3://foo/bar/baz") shouldBe 1
  }

  "source" should "should get input stream of path" in {
    s3Client.putObject(
      PutObjectRequest.builder
        .bucket("foo")
        .key("bar/baz").build, in)
    val lines = new BufferedReader(
      new InputStreamReader(
        new S3(s3Client).source("s3://foo/bar/baz")
      )
    ).lines.iterator.asScala.toList
    lines shouldBe List("1")
  }

  "sink" should "should get output stream of path" in {
    val printer = new PrintWriter(
      new S3(s3Client).sink("s3://foo/bar/baz"))
    printer.print("2")
    printer.close

    val lines = new BufferedReader(
      new InputStreamReader(
        new S3(s3Client).source("s3://foo/bar/baz")
      )
    ).lines.iterator.asScala.toList
    lines shouldBe List("2")
  }
}

