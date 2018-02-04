package me.ngrid.crimson.client.graphics.algebras

//TODO definitely needs to be rethought once i know more.
trait PrimitivesAlg[F[_], ShaderProgram, Primitive] {
  def createPoint(shader: ShaderProgram, size: Float): F[Primitive]
  def createTriangle(shader: ShaderProgram): F[Primitive]
}

