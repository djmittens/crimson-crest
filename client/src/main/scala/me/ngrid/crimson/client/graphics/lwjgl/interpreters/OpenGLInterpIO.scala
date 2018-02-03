package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.effect.IO
import me.ngrid.crimson.client.graphics.algebras.opengl.{GL11Alg, GL20Alg, GL30Alg, GL45Alg}
import org.lwjgl.opengl.{GL, GLCapabilities, _}

object OpenGLInterpIO
  extends GL11Alg[IO]
    with GL20Alg[IO]
    with GL30Alg[IO]
    with GL45Alg[IO] {

  override def clearColor(red: Float, green: Float, blue: Float, alpha: Float): IO[Unit] = IO {
    // Set the clear color
    GL11.glClearColor(red, green, blue, alpha)
  }

  override def clear(flags: Int): IO[Unit] = IO {
    // GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT
    GL11.glClear(flags) // clear the framebuffer
  }

  override def createCapabilities: IO[GLCapabilities] = IO {
    GL.createCapabilities()
  }

  override def pointSize(size: Float): IO[Unit] = IO {
    GL11.glPointSize(size)
  }

  def drawArrays(mode: Int, first: Int, count: Int): IO[Unit] = IO {
    GL11.glDrawArrays(mode, first, count)
  }

  def compileShader(shader: Int): IO[Unit] = IO {
    GL20.glCompileShader(shader)
  }

  def createShader(t: Int): IO[Int] = IO {
    GL20.glCreateShader(t)
  }

  def shaderSource(shader: Int, src: String): IO[Unit] = IO {
    GL20.glShaderSource(shader, src)
  }

  def createProgram(): IO[Int] = IO {
    GL20.glCreateProgram()
  }

  def attachShader(program: Int, shader: Int): IO[Unit] = IO {
    GL20.glAttachShader(program, shader)
  }

  def linkProgram(program: Int): IO[Unit] = IO {
    GL20.glLinkProgram(program)
  }

  def useProgram(program: Int): IO[Unit] = IO {
    GL20.glUseProgram(program)
  }


  def deleteShader(shader: Int): IO[Unit] = IO {
    GL20.glDeleteShader(shader)
  }

  def deleteProgram(program: Int): IO[Unit] = IO {
    GL20.glDeleteProgram(program)
  }

  /**
    * These commands clear a specified buffer of a framebuffer to specified value(s). For glClearBuffer*, the framebuffer is the currently bound draw framebuffer object. For glClearNamedFramebuffer*, framebuffer is zero, indicating the default draw framebuffer, or the name of a framebuffer object. buffer and drawbuffer identify the buffer to clear.
    */
  override def clearBufferfv(buffer: Int, drawBuffer: Int, value: Array[Float]): IO[Unit] = IO {
    // GL_COLOR
    GL30.glClearBufferfv(buffer, drawBuffer, value)
  }


  def bindVertexArray(array: Int): IO[Unit] = IO {
    GL30.glBindVertexArray(array)
  }

  def deleteVertexArrays(array: Int): IO[Unit] = IO {
    GL30.glDeleteVertexArrays(array)
  }

  def createVertexArrays(): IO[Int] = IO {
    GL45.glCreateVertexArrays()
  }

}
