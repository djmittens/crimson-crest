package me.ngrid.crimson.api.graphics

import me.ngrid.crimson.api.graphics.WindowAlg.OpenGLApiVersion

trait WindowAlg[F[_]] {
  type Window

  def init(): F[Unit]

  def createOpenGLWindow(version: OpenGLApiVersion, settings: WindowAlg.WindowSettings): F[Window]

  def close(w: Window): F[Unit]

  def interceptClose(w: Window)(f: Window => Unit): F[Unit]

  def renderLoop(window: Window)(loop: RenderLoopAlg[F]): F[Unit]
}

object WindowAlg {
  type Aux[F[_], W] = WindowAlg[F] {type Window = W}

  case class WindowSettings (
                            height: Int,
                            width: Int,
                            title: String
                            )

  sealed trait OpenGLApiVersion {
    val major: Int
    val minor: Int
  }

  object GL33 extends OpenGLApiVersion {
    override val major: Int = 3
    override val minor: Int = 3
  }
}