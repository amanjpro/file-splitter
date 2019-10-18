package me.amanj.file.splitter.fs

import org.scalatest._

class SftpSpec extends FlatSpec with Matchers {
  val login = new Sftp.Login("user", "pass")
  "separator" should "be /" in {
    new Sftp(login).separator shouldBe "/"
  }

  "extractFilePath" should "extract the file path when port is present" in {
    val path = "sftp://host:1/ak/am"
    new Sftp(login).extractFilePath(path) shouldBe path
  }

  "extractFilePath" should "extract the file path when port is absent" in {
    val path = "sftp://host/ak/am"
    new Sftp(login).extractFilePath(path) shouldBe path
  }

  "extractFilePath" should "extract the file path when path is in home" in {
    val path = "sftp://host:2/ak"
    new Sftp(login).extractFilePath(path) shouldBe path
  }

  "remoteFile" should "extract the file path when port is present" in {
    new Sftp(login).remoteFile("sftp://host:1/ak/am") shouldBe "ak/am"
  }

  "remoteFile" should "extract the file path when port is absent" in {
    new Sftp(login).remoteFile("sftp://host/ak/am") shouldBe "ak/am"
  }

  "remoteFile" should "extract the file path when path is in home" in {
    new Sftp(login).remoteFile("sftp://host:2/ak") shouldBe "ak"
  }

  "port" should "extract the port number" in {
    new Sftp(login).port("sftp://host:2/ak") shouldBe 2
  }

  "port" should "return 22 when the port is not specified" in {
    new Sftp(login).port("sftp://host/ak") shouldBe 22
  }

  "host" should "extract the host name when port is present" in {
    new Sftp(login).host("sftp://host:1/ak/am") shouldBe "host"
  }

  "host" should "extract the host name when port is absent" in {
    new Sftp(login).host("sftp://host/ak/am") shouldBe "host"
  }

  "host" should "extract the host name when path is in home" in {
    new Sftp(login).host("sftp://host:2/ak") shouldBe "host"
  }
}

