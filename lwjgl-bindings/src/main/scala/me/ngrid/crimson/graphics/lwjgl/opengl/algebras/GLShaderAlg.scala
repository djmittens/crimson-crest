package me.ngrid.crimson.graphics.lwjgl.opengl.algebras

import me.ngrid.crimson.graphics.lwjgl.opengl.algebras.GLShaderAlg._

trait GLShaderAlg[F[_], Err] {
  def link(program: UnlinkedProgram): F[Either[Err, LinkedProgram]]
  def compile(compile: ShaderSource): F[Either[Err, CompiledShader]]
}
object GLShaderAlg {
  case class ShaderSource(source: String, kind: ShaderKind)
  case class CompiledShader(ptr: Int, kind: ShaderKind)

  case class UnlinkedProgram(shaders: List[CompiledShader])
  case class LinkedProgram(ptr: Int)

  sealed trait ShaderKind
  case object VertexShader extends ShaderKind
  case object FragmentShader extends ShaderKind
}
