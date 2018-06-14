package me.ngrid

import cats.effect.IO
import me.ngrid.crimson.client.graphics.lwjgl.interpreters.{GLSimpleLoopIO, GlfwInterpIO}
import org.lwjgl._
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._

import scala.concurrent.ExecutionContext
//import cats.syntax._
import cats.implicits._

object LwjglHelloWorld {
  def main(args: Array[String]): Unit = {
    println(s"Hello LWJGL ${Version.getVersion}!")

    val glfw = GlfwInterpIO

    val game = for {
      w <- glfw.createOpenGL()
      _ <- glfw.renderLoop(w, GLSimpleLoopIO[Unit](
        init = _ => IO.unit,
        render = _ => _ => IO {
          GL11.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
          ()
        },
        terminate = _ => _ => IO.unit
      ))
      _ <- glfw.close(w)
    } yield ()

    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(r => r.run())

    (IO.shift *> game).attempt.unsafeRunSync() match {
      case Left(e) => println(s"Failed with an error $e")
      case Right(v) => println(s"Success $v")
    }
    ()
  }
}
