package me.ngrid.crimson.client.graphics.lwjgl.algebras

trait GLShaderAlg[F[_], Err] {
  def vertex(source: String): F[Either[Err, GLShader[F]]]
  def fragment(source: String): F[Either[Err, GLShader[F]]]
  def createShaderProgram(s: List[GLShader[F]]): F[Either[Err, GLShaderProgram[F]]]
}

case class GLShader[F[_]](ptr: Int, source: String, delete: F[Unit])

case class GLShaderProgram[F[_]](ptr: Int, shaders: List[GLShader[F]], delete: F[Unit])

