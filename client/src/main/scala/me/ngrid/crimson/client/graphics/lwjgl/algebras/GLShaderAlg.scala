package me.ngrid.crimson.client.graphics.lwjgl.algebras

import cats.Monad
import cats.implicits._
import me.ngrid.crimson.client.graphics.algebras.OpenGlAlg

case class GLShaderAlg[F[_]: Monad](gl: OpenGlAlg[F]) {
  val GL_VERTEX_SHADER: Int = 0x8B31
  val GL_FRAGMENT_SHADER: Int = 0x8B30

  def vertex(source: String): F[Int] = createShader(GL_VERTEX_SHADER, source)

  def fragment(source: String): F[Int] = createShader(GL_FRAGMENT_SHADER, source)

  def createShader(shaderType: Int, source: String): F[Int] = for {
    sh <- gl.createShader(shaderType)
    _ <- gl.shaderSource(sh, source)
    _ <- gl.compileShader(sh)
  } yield sh
}
