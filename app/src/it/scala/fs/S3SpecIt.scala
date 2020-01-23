package me.amanj.file.splitter.fs

import org.scalatest._
import java.net.URI
import java.lang.Boolean.TRUE
import java.io.{File, PrintWriter, BufferedReader, InputStreamReader}
import java.nio.file.Files
import java.nio.file.{Files, Path}

// S3 imports
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials,
  StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{CreateBucketRequest,
  PutObjectRequest, DeleteBucketRequest}
import software.amazon.awssdk.http.SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.utils.AttributeMap

// Java interop
import scala.jdk.CollectionConverters._
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request

class S3SpecIt extends FlatSpec with
  Matchers with BeforeAndAfterEach {

  val credential = AwsBasicCredentials.create("key", "secret")

  val s3Client = S3Client
    .builder
    .region(Region.of("us-east-1"))
    .endpointOverride(URI.create("http://localhost:9090"))
    .credentialsProvider(
      StaticCredentialsProvider.create(credential))
    .httpClient(UrlConnectionHttpClient.builder()
      .buildWithDefaults(AttributeMap.builder()
        .put(TRUST_ALL_CERTIFICATES, TRUE)
      .build()))
    .build



  var in: Path = _
  var out: Path = _

  override def afterEach(): Unit = {
    s3Client.listObjectsV2(ListObjectsV2Request.builder.bucket("foo").build)
      .contents.forEach { obj =>
        s3Client.deleteObject(DeleteObjectRequest.builder.bucket("foo")
          .key(obj.key()).build)
      }
    s3Client.deleteBucket(
      DeleteBucketRequest.builder.bucket("foo").build)

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
      CreateBucketRequest.builder.bucket("foo").build)

    super.beforeEach
  }

  "exists" should "return false when object is not found" in {
    new S3(s3Client).exists("s3://foo/bar/baz") shouldBe false
  }

  it should "return true when object is found" in {
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

  "source" should "get input stream of path" in {
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

  "sink" should "get output stream of path" in {
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

