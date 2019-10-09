package me.amanj.file.splitter.fs

import java.io.{InputStream, OutputStream}

object StdIO extends FS {
  def source(path: String): InputStream = System.in
  def sink(path: String): OutputStream = System.out
  def separator: String = ""
  def extractFilePath(path: String): String = ""
  def size(path: String): Long = ???
}
