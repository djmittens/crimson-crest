package me.ngrid.crimson.api.graphics

trait RenderLoopAlg[F[_]] {
  /**
    * Internal graphics state.
    */
  type State

  def init(): F[State]

  def render(st: State): F[Unit]

  def terminate(st: State): F[Unit]
}

object RenderLoopAlg {
  type Aux[F[_], Context] = RenderLoopAlg[F] {type State = Context}


  def dynamic[F[_], Context]( _init: F[Context],
                              _render: Context => F[Unit],
                              _terminate: Context => F[Unit] ): Aux[F, Context] =
    new RenderLoopAlg[F] {

      type State = Context

      override def init(): F[State] = _init

      override def render(st: State): F[Unit] = _render(st)

      override def terminate(st: State): F[Unit] = _terminate(st)
    }

  def static[F[_], Context]( _init: F[Context],
                             _render: F[Unit],
                             _terminate: F[Unit] ): Aux[F, Context] =
    new RenderLoopAlg[F] {

      type State = Context

      override def init(): F[State] = _init

      override def render(st: State): F[Unit] = _render

      override def terminate(st: State): F[Unit] = _terminate
    }
}
