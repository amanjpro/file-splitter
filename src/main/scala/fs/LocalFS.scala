package me.amanj.file.splitter.fs

import java.io.{File, FileInputStream, OutputStream,
  FileOutputStream, InputStream}

class LocalFS extends FS {
  def source(path: String): InputStream =
    new FileInputStream(path)

  def sink(path: String): OutputStream =
    new FileOutputStream(path)

  def separator: String = File.separator
}


