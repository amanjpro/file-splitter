package me.amanj.file.splitter.fs

import java.io.{InputStream, PrintWriter, OutputStream, FileOutputStream}
import java.nio.file.Files

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest,
  PutObjectResponse, PutObjectRequest, ListObjectsV2Request}
import software.amazon.awssdk.core.sync.ResponseTransformer
import scala.collection.JavaConverters._

class S3(region: Region) extends FS {
  val s3Client = S3Client.builder().region(region).build();

  private val scheme = "s3://"
  private val bucket = "[^/]+"
  private val key = ".*"
  private val S3Regex = s"$scheme($bucket)/?($key)".r

  def bucket(path: String): String = path match {
    case S3Regex(bucket, _) => bucket
  }

  def key(path: String): String = path match {
    case S3Regex(_, key) => key
  }

  // S3 support
  def source(file: String): InputStream = {
    s3Client.getObject (
      GetObjectRequest.builder
        .bucket(bucket(file))
        .key(key(file))
        .build
    )
  }

  def sink(file: String): OutputStream = {
    new S3.S3OutputStream(s3Client, bucket(file), key(file))
  }

  def separator: String = "/"

  def extractFilePath(path: String): String = path

  def exists(path: String): Boolean = {
    val objects = s3Client.listObjectsV2(
        ListObjectsV2Request.builder
          .bucket(bucket(path))
          .prefix(key(path))
          .build
      ).contents.asScala
        .filter(_.key == key(path))
      objects.length == 1
  }


  def size(path: String): Long = s3Client.listObjectsV2(
      ListObjectsV2Request.builder
        .bucket(bucket(path))
        .prefix(key(path))
        .build
    ).contents.asScala.foldLeft(0L)(_ + _.size)
}
object S3 {
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
}
