package me.amanj.file.splitter

import me.amanj.file.splitter.args.Config

package object fs {
  val HDFSDefaultRootURI = "hdfs://localhost:8020"
  val HDFSDefaultUser = "hdfs"
  val HDFSDefaultHome = "/"

  val SupportedFS = Seq("hdfs://", "file://", "s3://")

  def getInputFS(config: Config): FS = {
    if(config.inputFile.startsWith("local://")) new LocalFS
    else if(config.inputFile.startsWith("hdfs://")) {
      val maybeFS = for {
        root <- config.inputHdfsRootURI
        user <- config.inputHdfsUser
        home <- config.inputHdfsHome
      } yield new HDFS(root, user, home)
      maybeFS.getOrElse(new HDFS)
    }
    else if(config.inputFile.startsWith("s3://"))
      config.s3InputRegion.map(new S3(_)) match {
        case Some(fs) => fs
        case _        => throw new MatchError("Please provide input S3 region")
      }
    else
      throw new MatchError(s"Unsupported file system ${config.inputFile}")
  }

  def getOutputFS(config: Config): FS = {
    if(config.outputDir.startsWith("local://")) new LocalFS
    else if(config.outputDir.startsWith("hdfs://")) {
      val maybeFS = for {
        root <- config.outputHdfsRootURI
        user <- config.outputHdfsUser
        home <- config.outputHdfsHome
      } yield new HDFS(root, user, home)
      maybeFS.getOrElse(new HDFS)
    }
    else if(config.outputDir.startsWith("s3://"))
      config.s3OutputRegion.map(new S3(_)) match {
        case Some(fs) => fs
        case _        => throw new MatchError("Please provide input S3 region")
      }
    else
      throw new MatchError(s"Unsupported file system ${config.outputDir}")
  }
}

