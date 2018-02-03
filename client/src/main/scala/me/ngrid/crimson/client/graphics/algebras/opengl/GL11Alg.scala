package me.ngrid.crimson.client.graphics.algebras.opengl

import org.lwjgl.opengl.GLCapabilities

trait GL11Alg[F[_]] {

  def clearColor(red: Float, green: Float, blue: Float, alpha: Float): F[Unit]

  def clear(flags: Int): F[Unit]

  //TODO: probably should remove this as it leaks lwjgl.
  def createCapabilities: F[GLCapabilities]

  def drawArrays(mode: Int, first: Int, count: Int): F[Unit]

  def pointSize(size: Float): F[Unit]
}
