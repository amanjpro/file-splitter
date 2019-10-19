package me.amanj.file.splitter.fs

import java.io.{File, FileInputStream, OutputStream,
  FileOutputStream, InputStream}

object LocalFS extends FS {
  def source(path: String): InputStream =
    new FileInputStream(path)

  def sink(path: String): OutputStream =
    new FileOutputStream(path)

  def separator: String = File.separator

  def extractFilePath(path: String): String =
    path.replaceFirst("^file://", "")

  def size(path: String): Long = new File(path).length

  def exists(path: String): Boolean =
    new File(path).exists
}


