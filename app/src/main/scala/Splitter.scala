package me.amanj.file.splitter

import java.io.{BufferedReader, PrintWriter}

trait Splitter {
  def split(printers: Array[PrintWriter]): Unit
}

class OrderedSplitter(input: BufferedReader,
    uncompressedSize: Long) extends Splitter {
  override def split(printers: Array[PrintWriter]): Unit = {
    val cutoff = uncompressedSize / printers.size
    var sinkIndex = 0
    var bytesRead = 0L
    input.lines.forEach { line =>
      printers(sinkIndex).println(line)
      // the last + 1 is for new line
      bytesRead += line.getBytes.length + 1
      if(bytesRead >= cutoff) {
        if(sinkIndex != printers.length -1) {
          bytesRead = 0L
          printers(sinkIndex).close
        }
        sinkIndex = (sinkIndex + 1).min(printers.length - 1)
      }
    }
    printers(printers.length - 1).close
    input.close
  }
}

class UnorderedSplitter(input: BufferedReader) extends Splitter {
  override def split(printers: Array[PrintWriter]): Unit = {
    var sinkIndex = 0
    input.lines.forEach { line =>
      printers(sinkIndex).println(line)
      sinkIndex = (sinkIndex + 1) % printers.length
    }
    printers.foreach { printer =>
      printer.flush
      printer.close
    }
    input.close
  }
}
