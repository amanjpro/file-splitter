package me.amanj.file.splitter.fs

import java.net.URI
import java.io.{InputStream, OutputStream}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

class HDFS(rootURI: String = HDFSDefaultRootURI,
  user: String = HDFSDefaultUser,
  home: String = HDFSDefaultHome) extends FS {
  private val hadoopConf = new Configuration

  val fileSystem: FileSystem = {
    hadoopConf.set("fs.defaultFS", rootURI)
    hadoopConf.set("fs.hdfs.impl",
      classOf[org.apache.hadoop.hdfs.DistributedFileSystem].getName())
    hadoopConf.set("fs.file.impl",
      classOf[org.apache.hadoop.fs.LocalFileSystem].getName())
    // Set HADOOP user
    System.setProperty("HADOOP_USER_NAME", user)
    System.setProperty("hadoop.home.dir", home)
    //Get the filesystem - HDFS
    FileSystem.get(URI.create(rootURI), hadoopConf)
  }

  def extractFilePath(path: String): String =
    path.replaceFirst("^hdfs://", "")

  def source(path: String): InputStream =
    fileSystem.open(new Path(path))

  def sink(path: String): OutputStream =
    fileSystem.create(new Path(path))

  def exists(path: String): Boolean =
    fileSystem.exists(new Path(path))

  def separator: String = Path.SEPARATOR
  def size(path: String) =
    fileSystem.getFileStatus(new Path(path)).getLen
}
