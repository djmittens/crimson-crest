package me.ngrid.crimson.api.assets

trait TextFileAlg[F[_]] {
  def readAsString(path: String): F[String]
}


