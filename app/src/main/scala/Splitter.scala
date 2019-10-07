package me.amanj.file.splitter

import java.io.{BufferedReader, PrintWriter}

object Splitter {
  class Ordered(input: BufferedReader, size: Long) {
    def split(printers: Array[PrintWriter]): Unit = {
      val cutoff = size / printers.size
      var sinkIndex = 0
      var bytesRead = 0L
      input.lines.forEach { line =>
        printers(sinkIndex).println(line)
        bytesRead += line.getBytes.length
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
  class Unordered(input: BufferedReader) {
    def split(printers: Array[PrintWriter]): Unit = {
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
}
