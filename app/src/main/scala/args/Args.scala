package me.amanj.file.splitter.args

import me.amanj.file.splitter.compression.Compression
import me.amanj.file.splitter.fs._
import java.io.File
import scopt.OptionParser
import software.amazon.awssdk.regions.Region

case class Config(
  input: String = "",
  output: String = "",
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
  keepOrder: Boolean = false,
  numberOfParts: Int = 1
)

object ParseArgs {
  def processCompression(original: Config,
      action: Config => Config,
      compression: String): Config =
    if(compression != "none") action(original)
    else original

  def validateCompression(value: String,
      success: => Either[String, Unit],
      failure: String => Either[String, Unit]): Either[String, Unit] =
    Compression.supportedCompressions.find(_ == value).map(_ => success)
      .getOrElse(failure(s"Unsupported compression type: $value"))

  def validateFS(value: String,
      success: => Either[String, Unit],
      failure: String => Either[String, Unit]): Either[String, Unit] = {
    if(SupportedFS.exists(value.startsWith(_))) success
    else
      failure(s"Unsupported filesystem: $value")
  }

  val pkg = getClass.getPackage
  val parser = new OptionParser[Config](pkg.getImplementationTitle()) {
    override def renderingMode = scopt.RenderingMode.OneColumn
    head(pkg.getImplementationTitle(), pkg.getImplementationVersion())

    opt[String]('i', "input")
      .required
      .validate(x => validateFS(x, success, failure))
      .action((x, c) => c.copy(input = x))
      .text("The file to be splitted. At this point, S3, local FS and HDFS are supported. The job can also read from stdin by simply passing 'stdin' as the input. Exmples: hdfs://..., s3://... and file:://...")

    opt[String]('o', "output")
      .required
      .validate(x => validateFS(x, success, failure))
      .action((x, c) => c.copy(output = x))
      .text("The directory where the splitted parts should go. At this point, S3, local FS and HDFS are supported. The job can also write to stdout by simply passing 'stdout' here. Exmples: hdfs://..., s3://... and file:://...")

    opt[String]('x', "input-compression")
      .validate(x => validateCompression(x, success, failure _))
      .action((x, c) =>
          processCompression(c, _.copy(inputCompression = Some(x)), x))
      .text("Input file compression formatSupported compressions: none, gzip. Default: none")

    opt[String]('z', "output-compression")
      .validate(x => validateCompression(x, success, failure _))
      .action((x, c) =>
          processCompression(c, _.copy(outputCompression = Some(x)), x))
      .text("Output file compression format Supported compressions: none, gzip Default: none")

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

    opt[Boolean]("keep-order")
      .action((x, c) => c.copy(keepOrder = x))
      .text("Keep the order of the input lines. That is first n lines go to the first file and so on. This might generate files with uneven sizes")

    opt[Int]('n', "number-of-files")
      .action((x, c) => c.copy(numberOfParts = x))
      .text("Number of output files, default is 1.")

    checkConfig( c =>
      if (c.input == "stdin" && c.keepOrder)
        failure("When input is stdin, --keep-order cannot be provided")
      else if (c.output == "stdout" && c.numberOfParts != 1)
        failure("When output is stdout, --number-of-files can only be 1")
      else if (c.input.startsWith("s3://") && c.s3InputRegion.isEmpty)
        failure("When input is s3, --s3-input-region should be provided")
      else if (!c.input.startsWith("s3://") && c.s3InputRegion.isDefined)
        failure("Only when input is s3, --s3-input-region should be provided")
      else if (c.output.startsWith("s3://") && c.s3OutputRegion.isEmpty)
        failure("When output is s3, --s3-output-region should be provided")
      else if (!c.output.startsWith("s3://") && c.s3OutputRegion.isDefined)
        failure("Only when output is s3, --s3-output-region should be provided")
      else if (c.input.startsWith("hdfs://") && (
        c.inputHdfsHome.isEmpty || c.inputHdfsUser.isEmpty
          || c.inputHdfsRootURI.isEmpty))
        failure("When input is hdfs, --input-hdfs-home-dir, --input-hdfs-user, and --input-hdfs-root-uri should be provided")
      else if (!c.input.startsWith("hdfs://") && (
        c.inputHdfsHome.isDefined || c.inputHdfsUser.isDefined
          || c.inputHdfsRootURI.isDefined))
        failure("Only when input is hdfs, --input-hdfs-home-dir, --input-hdfs-user, and --input-hdfs-root-uri should be provided")
      else if (c.output.startsWith("hdfs://") && (
        c.outputHdfsHome.isEmpty || c.outputHdfsUser.isEmpty
          || c.outputHdfsRootURI.isEmpty))
        failure("When output is hdfs, --output-hdfs-home-dir, --output-hdfs-user, and --output-hdfs-root-uri should be provided")
      else if (!c.output.startsWith("hdfs://") && (
        c.outputHdfsHome.isDefined || c.outputHdfsUser.isDefined
          || c.outputHdfsRootURI.isDefined))
        failure("Only when output is hdfs, --output-hdfs-home-dir, --output-hdfs-user, and --output-hdfs-root-uri should be provided")

      else success
    )
  }
}
