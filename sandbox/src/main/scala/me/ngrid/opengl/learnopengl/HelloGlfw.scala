package me.ngrid.opengl.learnopengl

import cats.effect.IO
import me.ngrid.crimson.api.graphics.RenderLoopAlg
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.{GLBackgroundIO, GLSimpleLoop, GlfwInterpIO}
import org.lwjgl.opengl.GL11

/**
  * The first tutorial on https://learnopengl.com/Getting-started/Hello-Window
  * This just sets up the window with OpenGL 3.3 ( base -> core profile that all subsequent tuorials will use.
  */
object HelloGlfw {
  def main(args: Array[String]): Unit = {
    val glfw = GlfwInterpIO
    val bg = GLBackgroundIO

    val m = for {
      _ <- glfw.init()
      w <- glfw.createOpenGLWindow()
      _ <- glfw.renderLoop(w, GLSimpleLoop(
        gl => RenderLoopAlg.dynamic[IO, Unit] (
          _init = bg(gl).get.setBackgroundToBlack(),
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
