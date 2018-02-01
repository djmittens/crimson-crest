package me.ngrid.crimson.client.graphics.algebras

trait WindowAlg[F[_]] {
  type Window

  def createOpenGL(): F[Window]

  def swapBuffers(w: Window): F[Unit]

  def close(w: Window): F[Unit]

  def interceptClose(w: Window, f: Window => Unit): F[Unit]

  def renderLoop[State](window: Window, loop: RenderLoopAlg[F, State]): F[Unit]
}

object WindowAlg {
  type Aux[F[_], W] = WindowAlg[F] {type Window = W}
}