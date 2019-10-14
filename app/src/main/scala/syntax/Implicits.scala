package me.amanj.file.splitter.syntax

import java.io.{Reader, InputStream, BufferedReader, Writer,
  PrintWriter, InputStreamReader, OutputStream, OutputStreamWriter}
import java.nio.charset.{Charset, StandardCharsets}
import me.amanj.file.splitter.compression.Compression
import me.amanj.file.splitter.{Splitter, UnorderedSplitter, OrderedSplitter}
import me.amanj.file.splitter.fs.FS

object Implicits {
  implicit class ReaderExt(self: Reader) {
    def buffered: BufferedReader =
      new BufferedReader(self)

    def sinks(printers: Array[PrintWriter]): Unit =
      buffered.sinks(printers)
  }

  implicit class BufferedReaderExt(self: BufferedReader) {
    def ordered(implicit fs: FS, path: String): Splitter =
      new OrderedSplitter(self, fs.size(path))

    def unordered(implicit path: String): Splitter =
      new UnorderedSplitter(self)
  }

  implicit class SplitterExt(self: Splitter) {
    def sinks(printers: Array[PrintWriter]): Unit =
      self.split(printers)
  }

  implicit class WriterExt(self: Writer) {
    def printer: PrintWriter =
      new PrintWriter(self)
  }
}

