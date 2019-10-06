package me.amanj.file.splitter.args

import me.amanj.file.splitter.fs._
import java.io.File
import scopt.OptionParser
import software.amazon.awssdk.regions.Region

case class Config(
  inputFile: String = "",
  outputDir: String = "",
  inputCompression: Option[String] = None,
  outputCompression: Option[String] = None,
  s3InputRegion: Option[Region] = None,
  s3OutputRegion: Option[Region] = None,
  inputHdfsRootURI: Option[String] = None,
  inputHdfsUser: Option[String] = None,
  inputHdfsHome: Option[String] = None,
  outputHdfsRootURI: Option[String] = None,
  outputHdfsUser: Option[String] = None,
  outputHdfsHome: Option[String] = None,
  numberOfParts: Int = 1
)

object ParseArgs {
  def processCompression(original: Config,
      action: Config => Config,
      compression: String): Config = {
    compression match {
      case "none" => original
      case "gzip" => action(original)
    }
  }

  def validateCompression(value: String,
      success: => Either[String, Unit],
      failure: String => Either[String, Unit]): Either[String, Unit] =
    value match {
      case "none" |
           "gzip" => success
      case _      => failure(s"Unsupported compression type: $value")
    }

  def validateFS(value: String,
      success: => Either[String, Unit],
      failure: String => Either[String, Unit]): Either[String, Unit] = {
    if(SupportedFS.exists(value.startsWith(_))) success
    else
      failure(s"Unsupported filesystem: $value")
  }

  val pkg = getClass.getPackage
  val parser = new OptionParser[Config](pkg.getImplementationTitle()) {
      head(pkg.getImplementationTitle(), pkg.getImplementationVersion())

      opt[String]('i', "input-file")
        .required
        .validate(x => validateFS(x, success, failure))
        .action((x, c) => c.copy(inputFile = x))
        .text(
          """|The file to be splitted.
             |At this point, S3, local FS and HDFS are supported
             |exmples: hdfs://..., s3://... and file:://...""".stripMargin)

      opt[String]('o', "output-dir")
        .required
        .validate(x => validateFS(x, success, failure))
        .action((x, c) => c.copy(outputDir = x))
        .text(
          """|The directory where the splitted parts should go.
             |At this point, S3, local FS and HDFS are supported
             |exmples: hdfs://..., s3://... and file:://...""".stripMargin)

      opt[String]('x', "input-compression")
        .validate(x => validateCompression(x, success, failure _))
        .action((x, c) =>
            processCompression(c, _.copy(inputCompression = Some(x)), x))
        .text(
          """|Input file compression format
             |Supported compressions: none, gzip.
             |Default: none""".stripMargin)

      opt[String]('z', "output-compression")
        .validate(x => validateCompression(x, success, failure _))
        .action((x, c) =>
            processCompression(c, _.copy(outputCompression = Some(x)), x))
        .text(
          """|Output file compression format
             |Supported compressions: none, gzip
             |Default: none""".stripMargin)

      opt[String]("s3-input-region")
        .action((x, c) => c.copy(s3InputRegion = Some(Region.of(x))))
        .text("Input S3 Region. Required when dealing with S3 paths only.")

      opt[String]("s3-output-region")
        .action((x, c) => c.copy(s3OutputRegion = Some(Region.of(x))))
        .text("Output S3 Region. Required when dealing with S3 paths only.")

      opt[String]("input-hdfs-root-uri")
        .action((x, c) => c.copy(inputHdfsRootURI = Some(x)))
        .text(s"Input HDFS root URI. Default: $HDFSDefaultRootURI")

      opt[String]("input-hdfs-user")
        .action((x, c) => c.copy(inputHdfsUser = Some(x)))
        .text(s"Input HDFS user. Default: $HDFSDefaultUser")

      opt[String]("input-hdfs-home-dir")
        .action((x, c) => c.copy(inputHdfsHome = Some(x)))
        .text(s"Input HDFS home directory. Default: $HDFSDefaultHome")

      opt[String]("output-hdfs-root-uri")
        .action((x, c) => c.copy(outputHdfsRootURI = Some(x)))
        .text(s"Output HDFS root URI. Default: $HDFSDefaultRootURI")

      opt[String]("output-hdfs-user")
        .action((x, c) => c.copy(outputHdfsUser = Some(x)))
        .text(s"Output HDFS user. Default: $HDFSDefaultUser")

      opt[String]("output-hdfs-home-dir")
        .action((x, c) => c.copy(outputHdfsHome = Some(x)))
        .text(s"Output HDFS home directory. Default: $HDFSDefaultHome")

      opt[Int]('n', "number-of-files")
        .required
        .action((x, c) => c.copy(numberOfParts = x))
        .text("Number of output files.")
  }
}
