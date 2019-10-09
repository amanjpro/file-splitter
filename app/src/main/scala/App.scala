package me.amanj.file.splitter

import me.amanj.file.splitter.fs._
import me.amanj.file.splitter.syntax.Implicits._
import me.amanj.file.splitter.args.{ParseArgs, Config}
import software.amazon.awssdk.regions.Region
import java.io.{PrintWriter, BufferedReader}

object App {
  def getPartNames(base: String, sep: String, parts: Int): Seq[String] =
    (0 until parts).map(i => f"$base${sep}part-$i%05d")

  def getSource(config: Config, fs: FS): BufferedReader = {
    val path = fs.extractFilePath(config.input)
    config.inputCompression match {
      case Some("gzip") =>
        fs.source(path).gzip.buffered
      case _      =>
        fs.source(path).buffered
    }
  }

  def getSinks(config: Config, fileNames: Seq[String], fs: FS): Seq[PrintWriter] =
    fileNames.map { file =>
      val path = fs.extractFilePath(file)
      config.outputCompression match {
        case Some("gzip") =>
          fs.sink(s"$path.gz").gzip.printer
        case _      =>
          fs.sink(path).printer
      }
    }

  def main(args: Array[String]): Unit = {
    ParseArgs.parser.parse(args, Config()) match {
      case Some(config) =>
        implicit val input = config.input
        implicit val inputFS = getInputFS(config)
        val outputFS = getOutputFS(config)
        val partNames =
          getPartNames(config.output, outputFS.separator,
            config.numberOfParts)

        val src = getSource(config, inputFS)
        val dest = getSinks(config, partNames, outputFS)

        if(config.keepOrder)
          src.ordered.sinks(dest.toArray)
        else
          src.sinks(dest.toArray)
      case _            => // do nothing
    }
  }
}
