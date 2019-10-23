package me.amanj.file.splitter.args

import org.scalatest.{Args => _, _}
import software.amazon.awssdk.regions.Region

class ArgsSpec extends FlatSpec with Matchers {
  "parser" should "parse fail to parse unsupported input paths" in {
    val args = Array("--input", "yay", "--output", "file://lll")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "parse fail to parse unsupported output paths" in {
    val args = Array("--output", "yay", "--input", "file://lll")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "parse stdio input/output paths" in {
    val args = Array("--input", "stdin", "--output", "stdout")
    val config = Config(input = "stdin", output="stdout")
    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "reject stdin as output" in {
    val args = Array("--input", "stdin", "--output", "stdin")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "reject stdout as input" in {
    val args = Array("--input", "stdout", "--output", "stdout")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "parse s3 input/output correctly" in {
    val args = Array("--input", "s3://1", "--output", "s3://2",
      "--s3-input-region", "a", "--s3-output-region", "b")
    val config = Config(input = "s3://1", output="s3://2",
      s3InputRegion = Some(Region.of("a")),
      s3OutputRegion = Some(Region.of("b")))
    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "fail if input s3 but no s3-input-region is provided" in {
    val args = Array("--input", "s3://1", "--output", "s3://2",
      "--s3-output-region", "b")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "fail if output s3 but no s3-output-region is provided" in {
    val args = Array("--input", "s3://1", "--output", "s3://2",
      "--s3-input-region", "b")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "parse hdfs input/output correctly" in {
    val args = Array("--input", "hdfs://1", "--output", "hdfs://2")
    val config = Config(input = "hdfs://1", output="hdfs://2")
    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "parse hdfs input/output correctly even if hdfs configs passed" in {
    val args = Array("--input", "hdfs://1", "--output", "hdfs://2",
      "--input-hdfs-root-uri", "ri",
      "--input-hdfs-user", "ui",
      "--input-hdfs-home-dir", "di",
      "--output-hdfs-root-uri", "ro",
      "--output-hdfs-user", "uo",
      "--output-hdfs-home-dir", "do")

    val config = Config(input = "hdfs://1", output="hdfs://2",
      inputHdfsRootURI = Some("ri"), inputHdfsUser = Some("ui"),
      inputHdfsHome = Some("di"), outputHdfsRootURI = Some("ro"),
      outputHdfsUser = Some("uo"), outputHdfsHome = Some("do"))

    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "parse local input/output files correctly" in {
    val args = Array("--input", "file://1", "--output", "file://2")
    val config = Config(input = "file://1", output="file://2")
    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "parse sftp input/output files correctly" in {
    val args = Array("--input", "sftp://1", "--output", "sftp://2",
      "--input-sftp-username", "ui", "--input-sftp-password", "pi",
      "--output-sftp-username", "uo", "--output-sftp-password", "po")
    val config = Config(input = "sftp://1", output="sftp://2",
      inputSftpUsername = Some("ui"), inputSftpPassword = Some("pi"),
      outputSftpUsername = Some("uo"), outputSftpPassword = Some("po"))
    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "parse sftp input/output files correctly - no password" in {
    val args = Array("--input", "sftp://1", "--output", "sftp://2",
      "--input-sftp-username", "ui", "--output-sftp-username", "uo")
    val config = Config(input = "sftp://1", output="sftp://2",
      inputSftpUsername = Some("ui"), outputSftpUsername = Some("uo"))
    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "fail sftp input with no username" in {
    val args = Array("--input", "sftp://1", "--output", "sftp://2",
      "--output-sftp-username", "uo")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "fail sftp output with no username" in {
    val args = Array("--input", "sftp://1", "--output", "sftp://2",
      "--input-sftp-username", "uo")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "parse gzip as compression" in {
    val args = Array("--input", "stdin", "--output", "stdout",
      "--input-compression", "gzip", "--output-compression", "gzip")
    val config = Config(input = "stdin", output="stdout",
      inputCompression = Some("gzip"),
      outputCompression = Some("gzip"))
    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "parse none as no-compression" in {
    val args1 = Array("--input", "stdin", "--output", "stdout",
      "--input-compression", "none", "--output-compression", "none")
    val config = Config(input = "stdin", output="stdout")
    Args.parser.parse(args1, Config()) shouldBe Some(config)
    val args2 = Array("--input", "stdin", "--output", "stdout")
    Args.parser.parse(args2, Config()) shouldBe Some(config)
  }

  it should "reject unsupported input-compressions" in {
    val args = Array("--input", "stdin", "--output", "stdout",
      "--input-compression", "bad")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "reject unsupported output-compressions" in {
    val args = Array("--input", "stdin", "--output", "stdout",
      "--output-compression", "bad")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "parse keep-order correctly" in {
    val args = Array("--input", "file://", "--output", "stdout",
      "--keep-order")
    val config = Config(input = "file://", output="stdout",
      keepOrder = true)
    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "reject keep-order with stdin" in {
    val args = Array("--input", "stdin", "--output", "stdout",
      "--keep-order")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "parse number-of-files correctly" in {
    val args = Array("--input", "file://", "--output", "file://",
      "--number-of-files", "2")
    val config = Config(input = "file://", output="file://",
      numberOfParts = 2)
    Args.parser.parse(args, Config()) shouldBe Some(config)
  }

  it should "reject number-of-file with stdout" in {
    val args = Array("--input", "file://", "--output", "stdout",
      "--number-of-files", "2")
    Args.parser.parse(args, Config()) shouldBe None
  }

  it should "reject bad hdfs config combinations" in {
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--input-hdfs-home-dir", "bad"), Config()) shouldBe None
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--output-hdfs-home-dir", "bad"), Config()) shouldBe None
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--input-hdfs-user", "bad"), Config()) shouldBe None
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--output-hdfs-user", "bad"), Config()) shouldBe None
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--input-hdfs-root-uri", "bad"), Config()) shouldBe None
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--output-hdfs-root-uri", "bad"), Config()) shouldBe None
  }

  it should "reject bad s3 config combinations" in {
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--s3-input-region", "bad"), Config()) shouldBe None
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--s3-output-region", "bad"), Config()) shouldBe None
  }

  it should "reject bad sftp config combinations" in {
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--input-sftp-username", "bad"), Config()) shouldBe None
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--input-sftp-password", "bad"), Config()) shouldBe None
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--outputput-sftp-username", "bad"), Config()) shouldBe None
    Args.parser.parse(
      Array("--input", "file://", "--output", "stdout",
        "--output-sftp-password", "bad"), Config()) shouldBe None
  }
}

