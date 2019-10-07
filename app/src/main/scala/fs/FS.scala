package me.amanj.file.splitter.fs

import java.io.{InputStream, OutputStream}

trait FS {
  def source(path: String): InputStream
  def sink(path: String): OutputStream
  def separator: String
  def extractFilePath(path: String): String
  def size(path: String): Long
}

