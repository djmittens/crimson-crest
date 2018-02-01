package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.Monad
import me.ngrid.crimson.client.graphics.algebras.{OpenGL11Alg, RenderLoopAlg}

class SimpleLoopGL[F[_]: Monad](gl: OpenGL11Alg[F])(override val render: F[Unit]) extends RenderLoopAlg[F] {
  override def init(): F[Unit] = Monad[F].pure {
    gl.createCapabilities
    ()
//    gl.clearColor(1.0f, 0f, 0f, 0f)
  }

  override def terminate(): F[Unit] = Monad[F].unit
}
