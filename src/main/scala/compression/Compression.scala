package me.amanj.file.splitter.compression

import java.io.{InputStream, Reader, InputStreamReader}
import java.util.zip.GZIPInputStream
import java.nio.charset.{Charset, StandardCharsets}

object Compression {
  // supported compressions
  def gzip(input: InputStream,
    charset: Charset = StandardCharsets.UTF_8 ): Reader = {
    val gzipStream = new GZIPInputStream(input)
    new InputStreamReader(gzipStream, charset)
  }
}


