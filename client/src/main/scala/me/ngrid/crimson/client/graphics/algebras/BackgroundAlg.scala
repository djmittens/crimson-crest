package me.ngrid.crimson.client.graphics.algebras

trait BackgroundAlg[F[_]] {
  def setBackgroundToBlack(): F[Unit]
}


