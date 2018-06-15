package me.ngrid.crimson.api.graphics

trait ViewportAlg[F[_]] {
  def setBackgroundColor(red: Float, green: Float, blue: Float, alpha: Float): F[Unit]
}
