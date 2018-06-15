package me.ngrid.crimson.graphics.lwjgl.opengl.interpreters

import cats.effect.IO
import me.ngrid.crimson.api.graphics.ViewportAlg
import org.lwjgl.opengl.{GL11, GLCapabilities}

object GLViewportIO {
  def apply(gl: GLCapabilities): Option[ViewportAlg[IO]] = {
    if(gl.glClearColor > 0) Some(gl11)
    else None
  }

  object gl11 extends ViewportAlg[IO] {

    override def setBackgroundColor(red: Float, green: Float, blue: Float, alpha: Float): IO[Unit] = IO {
      GL11.glClearColor(red, green, blue, alpha)
    }
  }

}
