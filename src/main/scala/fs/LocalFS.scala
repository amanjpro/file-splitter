package me.amanj.file.splitter.fs

import java.io.{FileInputStream, PrintWriter, FileOutputStream, InputStream}

class LocalFS extends FS {
  def source(path: String): InputStream =
    new FileInputStream(path)

  def sink(path: String): PrintWriter =
    new PrintWriter(new FileOutputStream(path))
}


