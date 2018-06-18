package me.ngrid.crimson.api.graphics.geometry

//TODO definitely needs to be rethought once i know more.
trait PrimitivesAlg[F[_], ShaderProgram, Primitive] {
  def createPoint(shader: ShaderProgram, size: Float): F[Primitive]
  def createTriangle(shader: ShaderProgram): F[Primitive]
  def createRectangle(shader: ShaderProgram): F[Primitive]
}

object PrimitivesAlg {
  case class Primitive[F[_]](vertexArrayPtr: Int, draw: F[Unit], delete: F[Unit])
}
