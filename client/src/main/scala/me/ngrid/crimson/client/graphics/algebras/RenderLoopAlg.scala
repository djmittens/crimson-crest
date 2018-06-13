package me.ngrid.crimson.client.graphics.algebras

trait RenderLoopAlg[F[_]] {
  /**
    * Internal graphics state.
    */
  type State

  def init(): F[State]
  def render(st: State): F[State]
  def terminate(st: State): F[Unit]
}
