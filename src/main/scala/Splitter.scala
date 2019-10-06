package me.amanj.file.splitter

import java.io.{BufferedReader, PrintWriter}

object Splitter {
  def split(input: BufferedReader, printers: Array[PrintWriter]): Unit = {
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
