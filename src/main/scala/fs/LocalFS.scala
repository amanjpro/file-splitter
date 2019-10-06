package me.amanj.file.splitter.fs

import java.io.{FileInputStream, OutputStream, FileOutputStream, InputStream}

class LocalFS extends FS {
  def source(path: String): InputStream =
    new FileInputStream(path)

  def sink(path: String): OutputStream =
    new FileOutputStream(path)
}


