package me.amanj.splitter

// IO imports
import java.io.{InputStream, OutputStream, FileInputStream, FileOutputStream,
  InputStreamReader, BufferedReader, PrintWriter, Reader}
import java.util.zip.GZIPInputStream
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Path, FileSystems}

// S3 imports
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest,
  PutObjectResponse, PutObjectRequest}
import software.amazon.awssdk.core.sync.ResponseTransformer

// HDFS imports
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem => HDFSFileSystem, Path => HDFSPath}

// Other imports
import java.net.URI

object App {
  val HDFSDefaultRootURI = "hdfs://localhost:8020"
  val HDFSDefaultUser = "hdfs"
  val HDFSDefaultHome = "/"

  def split(input: BufferedReader, printers: Array[PrintWriter]): Unit = {
    var sinkIndex = 0
    input.lines.forEach { line =>
      printers(sinkIndex).println(line)
      sinkIndex = (sinkIndex + 1) % printers.length
    }
    printers.foreach { printer =>
      printer.flush
      printer.close
    }
    input.close
  }

  // supported compression
  def gzip(input: InputStream,
    charset: Charset = StandardCharsets.UTF_8 ): Reader = {
    val gzipStream = new GZIPInputStream(input)
    new InputStreamReader(gzipStream, charset)
  }

  def buffered(reader: Reader): BufferedReader =
    new BufferedReader(reader)

  // local file system support
  def fromLocal(path: String): InputStream =
    new FileInputStream(path)

  def toLocal(path: String): PrintWriter =
    new PrintWriter(new FileOutputStream(path))

  // hdfs file system support
  def hdfsFileSystem(path: String)
    (rootURI: String =HDFSDefaultRootURI,
      user: String = HDFSDefaultUser,
      home: String = HDFSDefaultHome): HDFSFileSystem = {
    val conf = new Configuration
    conf.set("fs.defaultFS", rootURI)
    conf.set("fs.hdfs.impl",
      classOf[org.apache.hadoop.hdfs.DistributedFileSystem].getName())
    conf.set("fs.file.impl",
      classOf[org.apache.hadoop.fs.LocalFileSystem].getName())
    // Set HADOOP user
    System.setProperty("HADOOP_USER_NAME", user)
    System.setProperty("hadoop.home.dir", home)
    //Get the filesystem - HDFS
    HDFSFileSystem.get(URI.create(rootURI), conf)
  }

  def fromHDFS(fs: HDFSFileSystem, path: String): InputStream =
    fs.open(new HDFSPath(path))

  def toHDFS(fs: HDFSFileSystem, path: String): PrintWriter =
    new PrintWriter(fs.create(new HDFSPath(path)))

  // S3 support
  def fromS3(bucket: String, key: String,
    region: Region): InputStream = {
    val s3Client = S3Client.builder().region(region).build();
    s3Client.getObject (
      GetObjectRequest.builder
        .bucket(bucket)
        .key(key)
        .build
    )
  }

  def toS3(file: Path, bucket: String, key: String,
    region: Region): PutObjectResponse = {
    val s3Client = S3Client.builder().region(region).build();
    s3Client.putObject(
      PutObjectRequest.builder()
        .bucket(bucket).key(key)
       .build(),
     file)
  }

  def main(args: Array[String]): Unit = {
    val tmpPaths = (0 until 10).map { i =>
      f"/tmp/part-$i%03d"
    }
    val tmpOutputStreams = tmpPaths.map { path =>
      toLocal(path)
    }
    val input = buffered(gzip(fromS3("here", "there", null)))
    split(input, tmpOutputStreams.toArray)
    tmpPaths.foreach(p => toS3(FileSystems.getDefault.getPath(p), "", "", null))
  }
}
