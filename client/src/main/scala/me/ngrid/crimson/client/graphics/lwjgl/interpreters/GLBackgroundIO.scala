package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.effect.IO
import me.ngrid.crimson.client.graphics.algebras.BackgroundAlg
import org.lwjgl.opengl.{GL11, GLCapabilities}

object GLBackgroundIO {
  def apply(gl: GLCapabilities): Option[BackgroundAlg[IO]] = {
    if(gl.glClearColor > 0) Some(gl11)
    else None
  }

  object gl11 extends BackgroundAlg[IO] {
    override def setBackgroundToBlack(): IO[Unit] = IO {
      GL11.glClearColor(1.0f, 0f, 0f, 0f)
    }
  }

}
