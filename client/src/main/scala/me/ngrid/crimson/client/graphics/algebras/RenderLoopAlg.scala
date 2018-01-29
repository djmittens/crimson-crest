package me.ngrid.crimson.client.graphics.algebras

trait RenderLoopAlg[F[_]] {
  def init(): F[Unit]
  def render(): F[Unit]
  def terminate(): F[Unit]
}
