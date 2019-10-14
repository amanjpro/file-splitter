package me.amanj.file.splitter.syntax

import java.io.{Reader, InputStream, BufferedReader, Writer,
  PrintWriter, InputStreamReader, OutputStream, OutputStreamWriter}
import java.nio.charset.{Charset, StandardCharsets}
import me.amanj.file.splitter.compression.Compression
import me.amanj.file.splitter.Splitter
import me.amanj.file.splitter.fs.FS

object Implicits {
  implicit class ReaderExt(self: Reader) {
    def buffered: BufferedReader =
      new BufferedReader(self)

    def sinks(printers: Array[PrintWriter]): Unit =
      buffered.sinks(printers)
  }

  implicit class BufferedReaderExt(self: BufferedReader) {
    def sinks(printers: Array[PrintWriter]): Unit =
      new Splitter.Unordered(self).split(printers)

    def ordered(implicit fs: FS, path: String): Splitter.Ordered =
      new Splitter.Ordered(self, fs.size(path))
  }

  implicit class OrderedSplitterExt(self: Splitter.Ordered) {
    def sinks(printers: Array[PrintWriter]): Unit =
      self.split(printers)
  }

  implicit class WriterExt(self: Writer) {
    def printer: PrintWriter =
      new PrintWriter(self)
  }
}

