package me.ngrid.crimson.api.graphics

trait BackgroundAlg[F[_]] {
  def setBackgroundToBlack(): F[Unit]
}


