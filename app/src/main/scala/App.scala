package me.amanj.file.splitter

import me.amanj.file.splitter.fs._
import me.amanj.file.splitter.compression.Compression
import me.amanj.file.splitter.syntax.Implicits._
import me.amanj.file.splitter.args.{Args, Config}
import java.io.{PrintWriter, BufferedReader}

object App {
  private[splitter] def getPartNames(base: String, sep: String,
      parts: Int, ext: String): Seq[String] =
    (0 until parts).map(i => f"$base${sep}part-$i%05d$ext")

  private[splitter] def getSource(compression: Compression,
      input: String, fs: FS): BufferedReader = {
    val path = fs.extractFilePath(input)
    compression.reader(fs.source(path)).buffered
  }

  private[splitter] def getSinks(compression: Compression,
      fileNames: Seq[String], fs: FS): Seq[PrintWriter] =
    fileNames.map { file =>
      val path = fs.extractFilePath(file)
      compression.writer(fs.sink(s"$path")).printer
    }

  private[splitter] def run(config: Config): Unit = {
    // input stream
    val input = config.input
    val inputFS = getInputFS(config)
    val inFile = inputFS.extractFilePath(input)

    if(!inputFS.exists(inFile)) {
      println(s"$inFile does not exist, quitting...")
      System.exit(1)
    }

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

    if(partNames.forall(part =>
        !outputFS.exists(outputFS.extractFilePath(part)))) {
      val dest = getSinks(outCompression, partNames, outputFS)
      if(config.keepOrder)
        source
          .ordered(
            inCompression.compressionFactor * inputFS.size(inFile))
          .sinks(dest.toArray)
      else
        source
          .unordered
          .sinks(dest.toArray)
    } else {
      partNames
        .filter(part => outputFS.exists(outputFS.extractFilePath(part)))
        .foreach { part =>
          println(s"${outputFS.extractFilePath(part)} exists, cannot override...")
        }
      println("quitting")
      System.exit(2)
    }
  }

  def main(args: Array[String]): Unit =
    Args
      .parser
      .parse(args, Config())
      .map(run)
}
