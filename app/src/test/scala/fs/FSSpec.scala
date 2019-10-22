package me.amanj.file.splitter.fs

import me.amanj.file.splitter.args.Config
import software.amazon.awssdk.regions.Region
import org.scalatest._

class FSSpec extends FlatSpec with Matchers {

  val config = Config(
    input = "",
    output = "",
    s3InputRegion = Some(Region.of("us-west-1")),
    s3OutputRegion = Some(Region.of("us-west-1")),
    inputHdfsRootURI = Some("http://inputHdfsRootURI"),
    inputHdfsUser = Some("inputHdfsUser"),
    inputHdfsHome = Some("inputHdfsHome"),
    inputSftpUsername = Some("inputSftpUser"),
    inputSftpPassword = Some("inputSftpPassword"),
    outputHdfsRootURI = Some("http://outputHdfsRootURI"),
    outputHdfsUser = Some("outputHdfsUser"),
    outputHdfsHome = Some("outputHdfsHome"),
    outputSftpUsername = Some("outputSftpUsername"),
    outputSftpPassword = Some("outputSftpPassword"),
  )

  "getInputFS" should "get StdIO when input is stdin" in {
    getInputFS(config.copy(input = "stdin")) shouldBe StdIO
  }

  it should "get S3 when input starts with s3://" in {
    getInputFS(config.copy(input = "s3://")) shouldBe a [S3]
  }

  it should "get HDFS when input starts with hdfs:// even when no other option is passed" in {
    getInputFS(Config(input = "hdfs://")) shouldBe HDFS(
      HDFSDefaultRootURI, HDFSDefaultUser, HDFSDefaultHome)
  }

  it should "get HDFS when input starts with hdfs://" in {
    getInputFS(config.copy(input = "hdfs://")) shouldBe HDFS(
      "http://inputHdfsRootURI", "inputHdfsUser", "inputHdfsHome")
  }

  it should "get LocalFS when input starts with file://" in {
    getInputFS(config.copy(input = "file://")) shouldBe LocalFS
  }


  it should "get Sftp when input starts with sftp://" in {
    getInputFS(config.copy(input = "sftp://")) shouldBe Sftp(
      new Sftp.Login(config.inputSftpUsername.get,
        config.inputSftpPassword.get))
  }

  it should "get Sftp when input starts with sftp:// even if password is not given" in {
    getInputFS(config.copy(input = "sftp://",
      inputSftpPassword = None)) shouldBe Sftp(
      new Sftp.KeyAuth(config.inputSftpUsername.get))
  }

  "getOutputFS" should "get StdIO when output is stdout" in {
    getOutputFS(config.copy(output = "stdout")) shouldBe StdIO
  }

  it should "get S3 when output starts with s3://" in {
    getOutputFS(config.copy(output = "s3://")) shouldBe a [S3]
  }

  it should "get HDFS when output starts with hdfs:// even when no other option is passed" in {
    getOutputFS(Config(output = "hdfs://")) shouldBe HDFS(
      HDFSDefaultRootURI, HDFSDefaultUser, HDFSDefaultHome)
  }

  it should "get HDFS when output starts with hdfs://" in {
    getOutputFS(config.copy(output = "hdfs://")) shouldBe HDFS(
      "http://outputHdfsRootURI", "outputHdfsUser", "outputHdfsHome")
  }

  it should "get LocalFS when output starts with file://" in {
    getOutputFS(config.copy(output = "file://")) shouldBe LocalFS
  }


  it should "get Sftp when output starts with sftp://" in {
    getOutputFS(config.copy(output = "sftp://")) shouldBe Sftp(
      new Sftp.Login(config.outputSftpUsername.get,
        config.outputSftpPassword.get))
  }

  it should "get Sftp when output starts with sftp:// even if password is not given" in {
    getOutputFS(config.copy(output = "sftp://",
      outputSftpPassword = None)) shouldBe Sftp(
      new Sftp.KeyAuth(config.outputSftpUsername.get))
  }
}
