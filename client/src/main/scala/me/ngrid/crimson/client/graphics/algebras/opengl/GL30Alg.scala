package me.ngrid.crimson.client.graphics.algebras.opengl

trait GL30Alg[F[_]] {

  def clearBufferfv(buffer: Int, drawBuffer: Int, value: Array[Float]): F[Unit]

  def bindVertexArray(array: Int): F[Unit]

  def deleteVertexArrays(array: Int): F[Unit]
}
