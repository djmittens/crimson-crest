package me.ngrid.crimson.client.graphics.algebras.opengl

trait GL20Alg[F[_]] {
  def createShader(t: Int): F[Int]

  def shaderSource(shader: Int, src: String): F[Unit]

  def compileShader(shader: Int): F[Unit]

  def getShaderi(shader: Int, pname: Int): F[Int]

  def attachShader(program: Int, shader: Int): F[Unit]

  def getShaderInfoLog(shader: Int): F[String]

  def deleteShader(shader: Int): F[Unit]

  def createProgram(): F[Int]

  def linkProgram(program: Int): F[Unit]

  def getProgrami(program: Int, pname: Int): F[Int]

  def getProgramInfoLog(program: Int): F[String]

  def useProgram(program: Int): F[Unit]

  def deleteProgram(program: Int): F[Unit]

  def glVertexAttrib4fv(index: Int, v: Array[Float]): F[Unit]
}
