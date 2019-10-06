package me.amanj.file.splitter.fs

import java.io.{InputStream, OutputStream}

trait FS {
  def source(path: String): InputStream
  def sink(path: String): OutputStream
}

