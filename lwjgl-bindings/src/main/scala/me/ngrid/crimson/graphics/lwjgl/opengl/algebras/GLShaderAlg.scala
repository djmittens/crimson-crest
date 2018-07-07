package me.ngrid.crimson.graphics.lwjgl.opengl.algebras

trait GLShaderAlg[F[_], Err] {
  def link(program: UnlinkedProgram): F[Either[Err, LinkedProgram]]

  def fragment(source: CharSequence): F[Either[Err, CompiledShader[Shader.Fragment.type]]]

  def vertex(source: CharSequence): F[Either[Err, CompiledShader[Shader.Vertex.type]]]

  def delete(shader: CompiledShader[_]): F[Unit]

  def delete(program: LinkedProgram): F[Unit]

  type Pointer

  case class CompiledShader[T <: Shader](ptr: Pointer, kind: T)

  sealed trait Shader

  object Shader {
    case object Fragment extends Shader
    case object Vertex extends Shader
  }

  case class LinkedProgram(ptr: Pointer)

  case class UnlinkedProgram(
    fragmentShader: CompiledShader[Shader.Fragment.type],
    vertexShader: Option[CompiledShader[Shader.Vertex.type]] = None)

}
