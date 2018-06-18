package me.ngrid.crimson.graphics.lwjgl.opengl.algebras

import me.ngrid.crimson.graphics.lwjgl.opengl.algebras.GLShaderAlg._

trait GLShaderAlg[F[_], Err] {
  def link(program: UnlinkedProgram): F[Either[Err, LinkedProgram]]
  def compile(compile: ShaderSource): F[Either[Err, CompiledShader]]
}
object GLShaderAlg {
  sealed trait Shader
  case class ShaderSource(source: String, kind: ShaderKind)
  case class CompiledShader(ptr: Int, kind: ShaderKind)

  sealed trait ShaderKind
  case object VertexShader
  case object FragmentShader

  sealed trait ShaderProgram
  case class UnlinkedProgram(shaders: List[CompiledShader]) extends ShaderProgram
  case class LinkedProgram(ptr: Int) extends ShaderProgram
}
