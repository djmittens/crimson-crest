package me.ngrid.crimson.client.graphics.lwjgl.algebras

import me.ngrid.crimson.client.graphics.lwjgl.algebras.GLShaderAlg.{Shader, Program}

trait GLShaderAlg[F[_], Err] {
  def vertex(source: String): F[Either[Err, Shader[F]]]
  def fragment(source: String): F[Either[Err, Shader[F]]]
  def createShaderProgram(s: List[Shader[F]]): F[Either[Err, Program[F]]]
}
object GLShaderAlg {
  case class Shader[F[_]](ptr: Int, source: String, delete: F[Unit])

  case class Program[F[_]](ptr: Int, shaders: List[Shader[F]], delete: F[Unit])
}
