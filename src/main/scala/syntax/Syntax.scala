package me.amanj.file.splitter.syntax

import java.io.{Reader, InputStream, BufferedReader,
  PrintWriter, InputStreamReader}
import java.nio.charset.{Charset, StandardCharsets}
import me.amanj.file.splitter.compression.Compression
import me.amanj.file.splitter.Splitter

object Implicits {
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
}

