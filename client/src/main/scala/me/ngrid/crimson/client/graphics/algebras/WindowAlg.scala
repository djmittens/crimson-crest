package me.ngrid.crimson.client.graphics.algebras

trait WindowAlg[F[_]] {
  type Window

  def init(): F[Unit]

  def createOpenGL(): F[Window]

  def close(w: Window): F[Unit]

  def interceptClose(w: Window, f: Window => Unit): F[Unit]

  def renderLoop(window: Window, loop: RenderLoopAlg[F]): F[Unit]
}

object WindowAlg {
  type Aux[F[_], W] = WindowAlg[F] {type Window = W}
}