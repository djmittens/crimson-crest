package me.ngrid

import me.ngrid.crimson.client.graphics.lwjgl.RunGlfwApp
import me.ngrid.crimson.client.graphics.lwjgl.interpreters.{GlfwInterpIO, OpenGLInterpIO, GLSimpleLoopIO}
import org.lwjgl._
import org.lwjgl.opengl.GL11._
//import cats.syntax._
//import cats.implicits._

object LwjglHelloWorld {
  def main(args: Array[String]): Unit = {
    println(s"Hello LWJGL ${Version.getVersion}!")

    val glfw = GlfwInterpIO
    val gl = OpenGLInterpIO

    RunGlfwApp {
      for {
        w <- glfw.createOpenGL()
        _ <- glfw.renderLoop(w, new GLSimpleLoopIO(gl)(
          gl.clear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
        ))
        _ <- glfw.close(w)
      } yield ()
    }
  }
}
