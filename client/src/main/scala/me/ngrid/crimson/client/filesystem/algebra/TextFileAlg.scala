package me.ngrid.crimson.client.filesystem.algebra

trait TextFileAlg[F[_]] {
  def readAsString(path: String): F[String]
}


