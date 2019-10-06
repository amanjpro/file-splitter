package me.amanj.file.splitter

import me.amanj.file.splitter.fs._
import me.amanj.file.splitter.syntax.Implicits._
import me.amanj.file.splitter.args.{ParseArgs, Config}
import software.amazon.awssdk.regions.Region
import java.io.{PrintWriter, BufferedReader}

object App {
  def getPartNames(base: String, sep: String, parts: Int): Seq[String] =
    (0 to parts).map(i => s"$base$sep$i%05d")

  def getSource(config: Config, fs: FS): BufferedReader =
    config.inputCompression match {
      case Some("gzip") =>
        fs.source(config.inputFile).gzip.buffered
      case _      =>
        fs.source(config.inputFile).buffered
    }

  def getSinks(config: Config, fileNames: Seq[String], fs: FS): Seq[PrintWriter] =
    fileNames.map { file =>
      config.outputCompression match {
        case Some("gzip") =>
          fs.sink(file).gzip.printer
        case _      =>
          fs.sink(file).printer
      }
    }

  def main(args: Array[String]): Unit = {
    ParseArgs.parser.parse(args, Config()) match {
      case Some(config) =>
        val inputFS = getInputFS(config)
        val outputFS = getOutputFS(config)
        val partNames =
          getPartNames(config.outputDir, outputFS.separator,
            config.numberOfParts)

        val src = getSource(config, inputFS)
        val dest = getSinks(config, partNames, outputFS)

        src.sinks(dest.toArray)
      case _            => // do nothing
    }
  }
}
