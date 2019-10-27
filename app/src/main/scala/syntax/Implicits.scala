package me.amanj.file.splitter.syntax

import java.io.{Reader, BufferedReader, Writer, PrintWriter}
import me.amanj.file.splitter.compression.Compression
import me.amanj.file.splitter.{Splitter, UnorderedSplitter, OrderedSplitter}
import me.amanj.file.splitter.fs.FS

object Implicits {
  implicit class FSExt(self: FS) {
    def compression(tpe: Option[String]): Compression = {
      Compression.toCompression(tpe)
    }
  }

  implicit class ReaderExt(self: Reader) {
    def buffered: BufferedReader =
      new BufferedReader(self)
  }

  implicit class BufferedReaderExt(self: BufferedReader) {
    def ordered(size: Long): Splitter =
      new OrderedSplitter(self, size)

    def unordered: Splitter =
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

