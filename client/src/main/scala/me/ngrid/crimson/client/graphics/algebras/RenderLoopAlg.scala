package me.ngrid.crimson.client.graphics.algebras

trait RenderLoopAlg[F[_], State] {
  def init(): F[State]
  def render(st: State): F[Unit]
  def terminate(st: State): F[Unit]
}
