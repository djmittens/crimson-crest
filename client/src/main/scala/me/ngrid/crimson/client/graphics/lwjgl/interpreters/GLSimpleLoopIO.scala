package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.effect.IO
import me.ngrid.crimson.client.graphics.algebras.RenderLoopAlg
import org.lwjgl.opengl.{GL, GL11, GLCapabilities}

object GLSimpleLoopIO {
  def apply[Context](
             init: GLCapabilities => IO[Context],
             render: GLCapabilities => Context => IO[Context],
             terminate: GLCapabilities => Context => IO[Unit]
           ): IO[RenderLoopAlg[IO]{type State = Context}] = IO {
    lazy val _gl = GL.createCapabilities()
    lazy val _rnd = render(_gl)
    lazy val _tmte = terminate(_gl)
    val iit = init

    new RenderLoopAlg[IO] {
      type State = Context

      override def init(): IO[Context] = IO {
        GL11.glClearColor(1.0f, 0f, 0f, 0f)
        _gl
      }.flatMap(iit)

      override def render(st: Context): IO[Context] = _rnd(st)

      override def terminate(st: Context): IO[Unit] = _tmte(st)
    }
  }
}

