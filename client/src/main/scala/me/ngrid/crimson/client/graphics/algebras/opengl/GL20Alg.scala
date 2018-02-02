package me.ngrid.crimson.client.graphics.algebras.opengl

trait GL20Alg[F[_]] {
  def compileShader(shader: Int): F[Unit]

  def createShader(t: Int): F[Int]

  def shaderSource(shader: Int, src: String): F[Unit]

  def attachShader(program: Int, shader: Int): F[Unit]

  def deleteShader(shader: Int): F[Unit]

  def createProgram(): F[Int]

  def linkProgram(program: Int): F[Unit]

  def useProgram(program: Int): F[Unit]

  def deleteProgram(program: Int): F[Unit]
}
