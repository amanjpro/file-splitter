package me.amanj.file.splitter

import me.amanj.file.splitter.fs._
import me.amanj.file.splitter.compression.Compression
import me.amanj.file.splitter.syntax.Implicits._
import me.amanj.file.splitter.args.{ParseArgs, Config}
import java.io.{PrintWriter, BufferedReader}

object App {
  def getPartNames(base: String, sep: String,
      parts: Int, ext: String): Seq[String] =
    (0 until parts).map(i => f"$base${sep}part-$i%05d$ext")

  def getSource(compression: Compression,
      input: String, fs: FS): BufferedReader = {
    val path = fs.extractFilePath(input)
    compression.reader(fs.source(path)).buffered
  }

  def getSinks(compression: Compression,
      fileNames: Seq[String], fs: FS): Seq[PrintWriter] =
    fileNames.map { file =>
      val path = fs.extractFilePath(file)
      compression.writer(fs.sink(s"$path")).printer
    }

  def main(args: Array[String]): Unit = {
    ParseArgs.parser.parse(args, Config()) match {
      case Some(config) =>
        // input stream
        val input = config.input
        val inputFS = getInputFS(config)
        val inFile = inputFS.extractFilePath(input)
        val inCompression =
          Compression.toCompression(config.inputCompression)
        val source = getSource(inCompression, input, inputFS)

        // output streams
        val output = config.output
        val outputFS = getOutputFS(config)
        val outCompression =
          Compression.toCompression(config.outputCompression)
        val partNames =
          getPartNames(output, outputFS.separator,
            config.numberOfParts, outCompression.extension)
        val dest = getSinks(outCompression, partNames, outputFS)

        if(inputFS.exists(inFile) &&
          partNames.forall(! outputFS.exists(_))) {
          if(config.keepOrder)
            source
              .ordered(
                inCompression.compressionFactor * inputFS.size(inFile))
              .sinks(dest.toArray)
          else
            source
              .unordered
              .sinks(dest.toArray)
        } else if(! inputFS.exists(inFile)) {
          println(s"$inFile does not exist, quitting...")
          System.exit(1)
        } else {
          partNames.filter(outputFS.exists(_)).foreach { part =>
            println(s"$part exists, cannot override...")
          }
          println("quitting")
          System.exit(1)
        }
      case _            => // do nothing
    }
  }
}
