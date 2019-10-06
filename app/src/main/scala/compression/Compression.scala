package me.amanj.file.splitter.compression

import java.io.{InputStream, Reader, InputStreamReader,
  Writer, OutputStreamWriter, OutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.nio.charset.{Charset, StandardCharsets}

object Compression {
  // supported compressions
  def fromGzip(input: InputStream,
    charset: Charset = StandardCharsets.UTF_8 ): Reader = {
    val gzipStream = new GZIPInputStream(input)
    new InputStreamReader(gzipStream, charset)
  }

  // supported compressions
  def toGzip(input: OutputStream,
    charset: Charset = StandardCharsets.UTF_8): Writer = {
    val gzipStream = new GZIPOutputStream(input)
    new OutputStreamWriter(gzipStream, charset)
  }
}
