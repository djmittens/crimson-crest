package me.ngrid.crimson.api.filesystem.algebra

trait TextFileAlg[F[_]] {
  def readAsString(path: String): F[String]
}


