package me.amanj.file.splitter

import me.amanj.file.splitter.fs._
import me.amanj.file.splitter.compression.Compression
import me.amanj.file.splitter.syntax.Implicits._
import me.amanj.file.splitter.args.{ParseArgs, Config}
import software.amazon.awssdk.regions.Region
import java.io.{PrintWriter, BufferedReader}

object App {
  def getPartNames(base: String, sep: String, parts: Int): Seq[String] =
    (0 until parts).map(i => f"$base${sep}part-$i%05d")

  def getSource(compression: Compression,
      input: String, fs: FS): BufferedReader = {
    val path = fs.extractFilePath(input)
    compression.reader(fs.source(path)).buffered
  }

  def getSinks(compression: Compression,
      fileNames: Seq[String], fs: FS): Seq[PrintWriter] =
    fileNames.map { file =>
      val path = fs.extractFilePath(file)
      compression.writer(fs.sink(s"$path${compression.extension}")).printer
    }

  def main(args: Array[String]): Unit = {
    ParseArgs.parser.parse(args, Config()) match {
      case Some(config) =>
        implicit val input = config.input
        implicit val inputFS = getInputFS(config)

        val inCompression =
          Compression.toCompression(config.inputCompression)
        val outCompression =
          Compression.toCompression(config.outputCompression)

        val outputFS = getOutputFS(config)
        val partNames =
          getPartNames(config.output, outputFS.separator,
            config.numberOfParts)

        val src = getSource(inCompression, input, inputFS)
        val dest = getSinks(outCompression, partNames, outputFS)

        if(config.keepOrder)
          src.ordered.sinks(dest.toArray)
        else
          src.unordered.sinks(dest.toArray)
      case _            => // do nothing
    }
  }
}
