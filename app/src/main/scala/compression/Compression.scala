package me.amanj.file.splitter.compression

import java.io.{InputStream, Reader, InputStreamReader,
  Writer, OutputStreamWriter, OutputStream, BufferedReader,
  PrintWriter}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.nio.charset.{Charset, StandardCharsets}

trait Compression {
  def reader(input: InputStream, charset: Charset): Reader
  def reader(input: InputStream): Reader =
    reader(input, StandardCharsets.UTF_8)

  def writer(output: OutputStream, charset: Charset): Writer
  def writer(output: OutputStream): Writer =
    writer(output, StandardCharsets.UTF_8)

  def extension: String
  def compressionFactor: Int
}

object Compression {
  def toCompression(compression: Option[String]): Compression =
    compression match {
      case Some("gzip") => Gzip
      case None         => NoCompression
    }

  val supportedCompressions = Seq("none", "gzip")
}

object Gzip extends Compression {
  def reader(input: InputStream,
    charset: Charset): Reader = {
    val gzipStream = new GZIPInputStream(input)
    new InputStreamReader(gzipStream, charset)
  }

  def writer(output: OutputStream,
    charset: Charset): Writer = {
    val gzipStream = new GZIPOutputStream(output)
    new OutputStreamWriter(gzipStream, charset)
  }

  def extension: String = ".gz"

  def compressionFactor: Int = 10
}

object NoCompression extends Compression {
  // supported compressions
  def reader(input: InputStream,
    charset: Charset): Reader = {
    new BufferedReader(new InputStreamReader(input, charset))
  }

  // supported compressions
  def writer(output: OutputStream,
    charset: Charset): Writer = {
    new PrintWriter(new OutputStreamWriter(output, charset))
  }

  def extension: String = ""

  def compressionFactor: Int = 1
}
