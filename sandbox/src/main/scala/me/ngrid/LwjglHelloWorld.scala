package me.ngrid

import cats.effect.IO
import me.ngrid.crimson.api.graphics.{RenderLoopAlg, WindowAlg}
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.{GLSimpleLoop, GlfwInterpIO}
import org.lwjgl._
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._

//import scala.concurrent.ExecutionContext
//import cats.syntax._
//import cats.implicits._

object LwjglHelloWorld {
  def main(args: Array[String]): Unit = {
    println(s"Hello LWJGL ${Version.getVersion}!")

    val glfw = GlfwInterpIO

    val game = for {
      _ <- glfw.init()
      w <- glfw.createOpenGLWindow(WindowAlg.GL33, WindowAlg.WindowSettings(
        height = 300,
        width = 400,
        title = "Hello LWJGL!"
      ))
      _ <- glfw.renderLoop(w)(GLSimpleLoop[IO, Unit] { _ =>
        RenderLoopAlg.static(
          _init = IO {
            // Set the background color to red.
            GL11.glClearColor(1.0f, 0f, 0f, 0f)
          },
          _render = IO {
            // Clear our depth / color buffers i guess
            GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
          },
          _terminate = IO.unit
        )
      })
      _ <- glfw.close(w)
    } yield ()

    //    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(r => r.run())

    println(game.attempt.unsafeRunSync())
    ()
  }
}
