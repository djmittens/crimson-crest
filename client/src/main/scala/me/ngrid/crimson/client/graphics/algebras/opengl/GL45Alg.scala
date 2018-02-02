package me.ngrid.crimson.client.graphics.algebras.opengl

trait GL45Alg[F[_]] {
  def createVertexArrays(): F[Int]
}
