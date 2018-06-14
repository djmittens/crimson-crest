package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.Monad
import cats.implicits._
import me.ngrid.crimson.client.graphics.algebras.RenderLoopAlg
import org.lwjgl.opengl.{GL, GLCapabilities}

case class GLSimpleLoop[F[_]: Monad, Context]
(loop: GLCapabilities => RenderLoopAlg.Aux[F, Context])
  extends RenderLoopAlg[F] {

  /**
    * Internal graphics state.
    */
  override type State = Context

  private[this] val F = implicitly[Monad[F]]

  // FIXME: Eeeky but works so that means  i could probably refactor later.
  /**
    * Defer evaluation of the real loop until we have initialized the openGl context.
    */
  private[this] lazy val _other  =
    loop(GL.createCapabilities(true))

  override def init(): F[Context] = F.unit.flatMap { _ =>
    _other.init()
  }

  override def render(st: Context): F[Context] =
    _other.render(st)

  override def terminate(st: Context): F[Unit] =
    _other.terminate(st)
}

