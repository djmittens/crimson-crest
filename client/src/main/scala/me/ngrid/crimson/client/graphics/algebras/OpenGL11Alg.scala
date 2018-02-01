package me.ngrid.crimson.client.graphics.algebras

import org.lwjgl.opengl.GLCapabilities

trait OpenGL11Alg[F[_]] {
  def clearColor(red: Float, green: Float, blue: Float, alpha: Float): F[Unit]

  def clearBufferfv(buffer: Int, drawBuffer: Int, value: Array[Float]): F[Unit]

  def clear(flags: Int): F[Unit]

  def createCapabilities: F[GLCapabilities]
}
