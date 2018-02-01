package me.ngrid.crimson.client.graphics.algebras

import org.lwjgl.opengl.GLCapabilities

trait OpenGlAlg[F[_]] {
  def clearColor(red: Float, green: Float, blue: Float, alpha: Float): F[Unit]

  def clearBufferfv(buffer: Int, drawBuffer: Int, value: Array[Float]): F[Unit]

  def clear(flags: Int): F[Unit]

  def createCapabilities: F[GLCapabilities]

  def compileShader(shader: Int): F[Unit]

  def createShader(t: Int): F[Int]

  def shaderSource(shader: Int, src: String): F[Unit]

  def createProgram(): F[Int]

  def attachShader(program: Int, shader: Int): F[Unit]

  def linkProgram(program: Int): F[Unit]

  def deleteShader(shader: Int): F[Unit]
}
