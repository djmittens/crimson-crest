package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.Monad
import me.ngrid.crimson.client.graphics.algebras.{OpenGlAlg, RenderLoopAlg}

class SimpleLoopGL[F[_]: Monad](gl: OpenGlAlg[F])(r: F[Unit]) extends RenderLoopAlg[F, Unit] {
  override def init(): F[Unit] = Monad[F].pure {
    gl.createCapabilities
    ()
//    gl.clearColor(1.0f, 0f, 0f, 0f)
  }

  override def terminate(r: Unit): F[Unit] = Monad[F].unit

  override def render(st: Unit): F[Unit] = r
}
