package me.amanj.file.splitter

// IO imports
import java.io.{InputStream, OutputStream, FileInputStream, FileOutputStream,
  InputStreamReader, BufferedReader, PrintWriter, Reader}
import java.util.zip.GZIPInputStream
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Path, FileSystems}

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

trait FS {
  def source(path: String): InputStream
  def sink(path: String): PrintWriter
}

class LocalFS extends FS {
  def source(path: String): InputStream =
    new FileInputStream(path)

  def sink(path: String): PrintWriter =
    new PrintWriter(new FileOutputStream(path))
}

class S3OutputStream(s3Client: S3Client,
    bucket: String, key: String) extends OutputStream {

  private val tmpFile = Files.createTempFile("s3-output", "tmp")
  private val out = new FileOutputStream(tmpFile.toString)

  private[splitter] def commit(): PutObjectResponse = {
    out.close
    s3Client.putObject(
      PutObjectRequest.builder()
        .bucket(bucket).key(key)
       .build(),
     tmpFile)
  }

  override def write(value: Int): Unit = out.write(value)
  override def close(): Unit = {
    out.close
    commit
    tmpFile.toFile.delete
  }
}

object App {
  class HDFS(rootURI: String =HDFSDefaultRootURI,
        user: String = HDFSDefaultUser,
        home: String = HDFSDefaultHome) extends FS {

    private val fileSystem: HDFSFileSystem = {
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

    def source(path: String): InputStream =
      fileSystem.open(new HDFSPath(path))

    def sink(path: String): PrintWriter =
      new PrintWriter(fileSystem.create(new HDFSPath(path)))
  }

  class S3(region: Region) extends FS {
    val s3Client = S3Client.builder().region(region).build();

    def bucket(path: String): String =
      URI.create(path).getHost

    def key(path: String): String =
      URI.create(path).getPath

    // S3 support
    def source(file: String): InputStream = {
      s3Client.getObject (
        GetObjectRequest.builder
          .bucket(bucket(file))
          .key(key(file))
          .build
      )
    }

    def sink(file: String): PrintWriter = {
      new PrintWriter(new S3OutputStream(s3Client, bucket(file), key(file)))
    }
  }

  val HDFSDefaultRootURI = "hdfs://localhost:8020"
  val HDFSDefaultUser = "hdfs"
  val HDFSDefaultHome = "/"

  object Splitter {

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
  }

  object Compression {
    // supported compression
    def gzip(input: InputStream,
      charset: Charset = StandardCharsets.UTF_8 ): Reader = {
      val gzipStream = new GZIPInputStream(input)
      new InputStreamReader(gzipStream, charset)
    }
  }

  implicit class ReaderExt(self: Reader) {
    def buffered: BufferedReader =
      new BufferedReader(self)

    def sinks(printers: Array[PrintWriter]): Unit =
      buffered.sinks(printers)
  }

  implicit class BufferedReaderExt(self: BufferedReader) {
    def sinks(printers: Array[PrintWriter]): Unit =
      Splitter.split(self, printers)
  }

  implicit class InputStreamExt(self: InputStream) {
    def gzip(charset: Charset): Reader =
      Compression.gzip(self, charset)

    def gzip: Reader =
      this.gzip(StandardCharsets.UTF_8)

    def buffered: BufferedReader =
      new BufferedReader(new InputStreamReader(self))

    def sinks(printers: Array[PrintWriter]): Unit =
      buffered.sinks(printers)
  }


  def main(args: Array[String]): Unit = {
    val printers = (0 until 10).map { index =>
      new S3(Region.US_WEST_2).sink(s"s3://this/$index")
    }

    new S3(Region.US_WEST_2)
      .source("s3://here/there")
      .gzip
      .buffered
      .sinks(printers.toArray)
  }
}
