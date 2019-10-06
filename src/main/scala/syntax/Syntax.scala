package me.amanj.file.splitter.syntax

import java.io.{Reader, InputStream, BufferedReader, Writer,
  PrintWriter, InputStreamReader, OutputStream, OutputStreamWriter}
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

    def sinks(printers: Array[OutputStream]): Unit =
      Splitter.split(self, printers.map(_.printer))
  }

  implicit class InputStreamExt(self: InputStream) {
    def gzip(charset: Charset): Reader =
      Compression.fromGzip(self, charset)

    def gzip: Reader =
      gzip(StandardCharsets.UTF_8)

    def buffered: BufferedReader =
      new BufferedReader(new InputStreamReader(self))

    def sinks(printers: Array[PrintWriter]): Unit =
      buffered.sinks(printers)
  }

  implicit class OutputStreamExt(self: OutputStream) {
    def gzip(charset: Charset): Writer =
      Compression.toGzip(self, charset)

    def gzip: Writer =
      gzip(StandardCharsets.UTF_8)

    def printer: PrintWriter =
      new PrintWriter(new OutputStreamWriter(self))
  }

  implicit class WriterExt(self: Writer) {
    def printer: PrintWriter =
      new PrintWriter(self)
  }
}

