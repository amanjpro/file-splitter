package me.amanj.file.splitter.compression

import java.io.{FileInputStream, FileOutputStream}
import java.nio.file.Files
import org.scalatest._

class GzipSpec extends FlatSpec with Matchers {
  "compressionFactor" should "be 10" in {
    Gzip.compressionFactor shouldBe 10
  }

  "extension" should "be .gz" in {
    Gzip.extension shouldBe ".gz"
  }

  "gzip" should "be able to read what gzip files" in {
    val reader = Gzip.reader(getClass.getResourceAsStream("/test.gz"))
    var next = reader.read
    var actual = ""
    while(next != -1) {
      actual += next.toChar
      next = reader.read
    }
    actual shouldBe "1\n2\n3\n4\n"

  }

  it should "be able to read what it writes" in {
    val file = Files.createTempFile("test", "file")
    val writer = Gzip.writer(new FileOutputStream(file.toString))
    writer.write("hello world")
    writer.close
    val reader = Gzip.reader(new FileInputStream(file.toString))
    var actual = ""
    var next = reader.read
    while(next != -1) {
      actual += next.toChar
      next = reader.read
    }
    actual shouldBe "hello world"
  }
}

