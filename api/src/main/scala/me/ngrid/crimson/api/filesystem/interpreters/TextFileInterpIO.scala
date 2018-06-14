package me.ngrid.crimson.api.filesystem.interpreters

import cats.effect.IO
import me.ngrid.crimson.api.filesystem.algebra.TextFileAlg

import scala.io.Source

object TextFileInterpIO extends TextFileAlg[IO] {
  //TODO make this stuff async n stuff
  override def readAsString(path: String): IO[String] = IO {
    val stream = getClass.getResourceAsStream(path)
    if(stream == null) throw new IllegalArgumentException("Path was not found")
    val src = Source.fromInputStream(stream)
    try {
      src.mkString
    } finally {
      src.close()
      stream.close()
    }
  }
}
