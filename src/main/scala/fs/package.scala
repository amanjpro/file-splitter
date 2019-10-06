package me.amanj.file.splitter

package object fs {
  val HDFSDefaultRootURI = "hdfs://localhost:8020"
  val HDFSDefaultUser = "hdfs"
  val HDFSDefaultHome = "/"

  val SupportedFS = Seq("hdfs://", "file://", "s3://")
}

