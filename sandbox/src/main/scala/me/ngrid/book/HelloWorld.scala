package me.ngrid.book

import cats.effect.IO
import me.ngrid.crimson.client.graphics.lwjgl.RunGlfwApp
import me.ngrid.crimson.client.graphics.lwjgl.interpreters.{GlfwInterpIO, LwjglOpenGLIO, SimpleLoopGL}
import org.lwjgl.opengl._
import spire.implicits._
import spire.math._

object HelloWorld {
  def main(args: Array[String]): Unit = {

    val glfw = GlfwInterpIO
    val gl = LwjglOpenGLIO

    RunGlfwApp {
      for {
        w <- glfw.createOpenGL()
        _ <- glfw.renderLoop(w, new SimpleLoopGL(gl)(for {
          color <- IO {
            Array(nextColor(sin[Float]), nextColor(cos[Float]), 0.0f, 1.0f)
          }
          _ <- gl.clearBufferfv(GL11.GL_COLOR, 0, color)
          //            _ <- gl.clear(GL11.GL_COLOR_BUFFER_BIT)

        } yield ()
        ))
        _ <- glfw.close(w)
      } yield ()
    }
  }

  def nextColor(f: Float => Float): Float = {
    max(f(System.nanoTime() / 1000 / 1000 / 1000), 0) * 0.5f + 0.5f
  }

}
