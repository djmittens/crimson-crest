package me.ngrid.opengl

import cats.effect.IO
import me.ngrid.crimson.api.graphics.{RenderLoopAlg, WindowAlg}
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.GlfwInterpIO

case class SimpleWindow(renderLoop: RenderLoopAlg[IO]) {

  private[this] val glfw = GlfwInterpIO

  val program: IO[Long] = for {
    _ <- glfw.init()
    w <- glfw.createOpenGLWindow(WindowAlg.GL33, WindowAlg.WindowSettings(
      height = 300,
      width = 400,
      title = "Hello !!!"
    ))
    _ <- glfw.renderLoop(w)(renderLoop)
    _ <- glfw.close(w)
  } yield w
}
