package me.amanj.file.splitter

import me.amanj.file.splitter.fs.LocalFS
import me.amanj.file.splitter.syntax.Implicits._
import org.scalatest._
import java.io.{File, PrintWriter}
import java.nio.file.Files

class SplitterSpec extends FlatSpec with Matchers with BeforeAndAfterEach {
  var in: String = _
  var out1: String = _
  var out2: String = _

  override def beforeEach(): Unit = {
    in = Files.createTempFile("test", "input").toString
    out1 = Files.createTempFile("test", "out1").toString
    out2 = Files.createTempFile("test", "out2").toString

    val writer = new PrintWriter(in)
    writer.println("1")
    writer.println("2")
    writer.println("3")
    writer.println("4")
    writer.close
    super.beforeEach
  }

  override def afterEach(): Unit = {
    new File(in).delete
    new File(out1).delete
    new File(out2).delete
    super.afterEach
  }

  "Ordered.split" should "keep the input order when splitting" in {
    val input = LocalFS.compression(None)
      .reader(LocalFS.source(in)).buffered
    new OrderedSplitter(input, 8)
      .sinks(Array(new PrintWriter(out1), new PrintWriter(out2)))

    val lines1 = scala.io.Source.fromFile(out1).getLines.toSeq
    val lines2 = scala.io.Source.fromFile(out2).getLines.toSeq

    lines1 shouldBe Seq("1", "2")
    lines2 shouldBe Seq("3", "4")
  }

  "Unordered.split" should "use one printer per line" in {
    val input = LocalFS.compression(None)
      .reader(LocalFS.source(in)).buffered
    new UnorderedSplitter(input)
      .sinks(Array(new PrintWriter(out1), new PrintWriter(out2)))

    val lines1 = scala.io.Source.fromFile(out1).getLines.toSeq
    val lines2 = scala.io.Source.fromFile(out2).getLines.toSeq

    lines1 shouldBe Seq("1", "3")
    lines2 shouldBe Seq("2", "4")
  }
}
