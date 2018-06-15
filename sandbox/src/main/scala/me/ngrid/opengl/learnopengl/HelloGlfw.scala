package me.ngrid.opengl.learnopengl

import cats.effect.IO
import me.ngrid.crimson.api.graphics.{RenderLoopAlg, WindowAlg}
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.{GLSimpleLoop, GLViewportIO, GlfwInterpIO}
import org.lwjgl.opengl.GL11

/**
  * The first tutorial on https://learnopengl.com/Getting-started/Hello-Window
  * This just sets up the window with OpenGL 3.3 ( base -> core profile that all subsequent tuorials will use.
  */
object HelloGlfw {
  def main(args: Array[String]): Unit = {
    val glfw = GlfwInterpIO
    val bg = GLViewportIO

    val m = for {
      _ <- glfw.init()
      w <- glfw.createOpenGLWindow(WindowAlg.GL33, WindowAlg.WindowSettings(
        height = 300,
        width = 400,
        title = "Hello Glfw!!!"
      ))
      _ <- glfw.renderLoop(w)(GLSimpleLoop(
        gl => RenderLoopAlg.dynamic[IO, Unit] (
          _init = bg(gl).fold(IO.unit)(_.setBackgroundColor(0.2f, 0.3f, 0.3f, 1.0f)),
          _render = _ => IO {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
            ()
          },
          _terminate =  _ => IO.unit
        )
      ))
      _ <- glfw.close(w)
    } yield w

    m.unsafeRunSync()
    ()
  }

}
