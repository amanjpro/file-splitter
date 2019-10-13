package me.amanj.file.splitter.fs

import org.scalatest._

class SftpSpec extends FlatSpec with Matchers {
  "separator" should "be /" in {
    new Sftp("user", "pass").separator shouldBe "/"
  }

  "extractFilePath" should "extract the file path when port is present" in {
    val path = "sftp://host:1/ak/am"
    new Sftp("user", "pass").extractFilePath(path) shouldBe path
  }

  "extractFilePath" should "extract the file path when port is absent" in {
    val path = "sftp://host/ak/am"
    new Sftp("user", "pass").extractFilePath(path) shouldBe path
  }

  "extractFilePath" should "extract the file path when path is in home" in {
    val path = "sftp://host:2/ak"
    new Sftp("user", "pass").extractFilePath(path) shouldBe path
  }

  "remoteFile" should "extract the file path when port is present" in {
    new Sftp("user", "pass").remoteFile("sftp://host:1/ak/am") shouldBe "ak/am"
  }

  "remoteFile" should "extract the file path when port is absent" in {
    new Sftp("user", "pass").remoteFile("sftp://host/ak/am") shouldBe "ak/am"
  }

  "remoteFile" should "extract the file path when path is in home" in {
    new Sftp("user", "pass").remoteFile("sftp://host:2/ak") shouldBe "ak"
  }

  "port" should "extract the port number" in {
    new Sftp("user", "pass").port("sftp://host:2/ak") shouldBe 2
  }

  "port" should "return 22 when the port is not specified" in {
    new Sftp("user", "pass").port("sftp://host/ak") shouldBe 22
  }

  "host" should "extract the host name when port is present" in {
    new Sftp("user", "pass").host("sftp://host:1/ak/am") shouldBe "host"
  }

  "host" should "extract the host name when port is absent" in {
    new Sftp("user", "pass").host("sftp://host/ak/am") shouldBe "host"
  }

  "host" should "extract the host name when path is in home" in {
    new Sftp("user", "pass").host("sftp://host:2/ak") shouldBe "host"
  }
}

