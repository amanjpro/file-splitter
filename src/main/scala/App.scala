package me.amanj.file.splitter

import me.amanj.file.splitter.fs.S3
import me.amanj.file.splitter.syntax.Implicits._
import software.amazon.awssdk.regions.Region

object App {

  def main(args: Array[String]): Unit = {
    val printers = (0 until 10).map { index =>
      new S3(Region.US_WEST_2)
        .sink(s"s3://this/$index")
        .gzip
        .printer
    }

    new S3(Region.US_WEST_2)
      .source("s3://here/there")
      .gzip
      .buffered
      .sinks(printers.toArray)
  }
}
