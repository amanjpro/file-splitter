package me.amanj.file.splitter

import me.amanj.file.splitter.args.Config

package object fs {
  val HDFSDefaultRootURI = "hdfs://localhost:8020"
  val HDFSDefaultUser = "hdfs"
  val HDFSDefaultHome = "/"

  val SupportedFS = Seq("hdfs://", "file://", "s3://", "sftp", "stdout", "stdin")

  def getInputFS(config: Config): FS = {
    if(config.input == "stdin") StdIO
    else if(config.input.startsWith("file://")) LocalFS
    else if(config.input.startsWith("hdfs://")) {
      val maybeFS = for {
        root <- config.inputHdfsRootURI
        user <- config.inputHdfsUser
        home <- config.inputHdfsHome
      } yield new HDFS(root, user, home)
      maybeFS.getOrElse(new HDFS)
    } else if(config.input.startsWith("s3://"))
      config.s3InputRegion.map(new S3(_)) match {
        case Some(fs) => fs
        case _        => throw new MatchError("Please provide input S3 region")
      }
    else if(config.input.startsWith("sftp://")) {
      val conn = for {
        username <- config.inputSftpUsername
        password <- config.inputSftpPassword
      } yield new Sftp(username, password)
      conn match {
        case Some(fs) => fs
        case _        =>
          throw new MatchError(
            s"Please provide username and password for ${config.input}")
      }
    } else
      throw new MatchError(s"Unsupported file system ${config.input}")
  }

  def getOutputFS(config: Config): FS = {
    if(config.output == "stdout") StdIO
    else if(config.output.startsWith("file://")) LocalFS
    else if(config.output.startsWith("hdfs://")) {
      val maybeFS = for {
        root <- config.outputHdfsRootURI
        user <- config.outputHdfsUser
        home <- config.outputHdfsHome
      } yield new HDFS(root, user, home)
      maybeFS.getOrElse(new HDFS)
    } else if(config.output.startsWith("s3://"))
      config.s3OutputRegion.map(new S3(_)) match {
        case Some(fs) => fs
        case _        => throw new MatchError("Please provide output S3 region")
      }
    else if(config.output.startsWith("sftp://")) {
      val maybeSftp = for {
        username <- config.outputSftpUsername
        password <- config.outputSftpPassword
      } yield new Sftp(username, password)
      maybeSftp match {
        case Some(fs) => fs
        case _        =>
          throw new MatchError(
            s"Please provide username and password for ${config.output}")
      }
    } else
      throw new MatchError(s"Unsupported file system ${config.output}")
  }
}

