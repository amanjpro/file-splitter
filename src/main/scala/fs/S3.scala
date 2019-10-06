package me.amanj.file.splitter.fs

import java.net.URI
import java.io.{InputStream, PrintWriter, OutputStream, FileOutputStream}
import java.nio.file.Files

import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest,
  PutObjectResponse, PutObjectRequest}
import software.amazon.awssdk.core.sync.ResponseTransformer

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

  def sink(file: String): OutputStream = {
    new S3.S3OutputStream(s3Client, bucket(file), key(file))
  }
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
