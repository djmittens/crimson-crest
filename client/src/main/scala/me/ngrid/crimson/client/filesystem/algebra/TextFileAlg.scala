package me.ngrid.crimson.client.filesystem.algebra

import cats.effect.IO

import scala.io.Source

trait TextFileAlg[F[_]] {
  def readAsString(path: String): F[String]
}

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
