package me.amanj.file.splitter.compression

import java.io.{FileInputStream, FileOutputStream}
import java.nio.file.Files
import org.scalatest._

class NoCompressionSpec extends FlatSpec with Matchers {
  "compressionFactor" should "1" in {
    NoCompression.compressionFactor shouldBe 1
  }

  "extension" should "" in {
    NoCompression.extension shouldBe ""
  }

  "no-compression" should "be able to read what it writes" in {
    val file = Files.createTempFile("test", "file")
    val writer = NoCompression.writer(new FileOutputStream(file.toString))
    writer.write("hello world")
    writer.close
    val reader = NoCompression.reader(new FileInputStream(file.toString))
    var actual = ""
    var next = reader.read
    while(next != -1) {
      actual += next.toChar
      next = reader.read
    }
    actual shouldBe "hello world"
  }
}

