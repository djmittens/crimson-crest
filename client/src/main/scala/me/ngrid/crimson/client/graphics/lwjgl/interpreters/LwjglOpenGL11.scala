package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.effect.IO
import me.ngrid.crimson.client.graphics.algebras.OpenGL11Alg
import org.lwjgl.opengl.GL11.{glClear, glClearColor}
import org.lwjgl.opengl.{GL, GLCapabilities}

object LwjglOpenGL11 extends OpenGL11Alg[IO] {
  override def clearColor(red: Float, green: Float, blue: Float, alpha: Float): IO[Unit] = IO {
    // Set the clear color
    glClearColor(red, green, blue, alpha)
  }

  override def clear(flags: Int): IO[Unit] = IO {
    // GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT
    glClear(flags) // clear the framebuffer
  }

  override def createCapabilities: IO[GLCapabilities] = IO {
    GL.createCapabilities()
  }
}
