package me.ngrid.book

import cats.effect.IO
import cats.implicits._
import me.ngrid.crimson.client.graphics.algebras.RenderLoopAlg
import me.ngrid.crimson.client.graphics.lwjgl.RunGlfwApp
import me.ngrid.crimson.client.graphics.lwjgl.algebras.GLShaderAlg
import me.ngrid.crimson.client.graphics.lwjgl.interpreters.{GlfwInterpIO, OpenGLInterpIO}
import org.lwjgl.opengl._
import spire.implicits._
import spire.math._

object HelloWorld {
  private val glfw = GlfwInterpIO
  private val gl = OpenGLInterpIO
  private val glShader = GLShaderAlg(gl)

  def main(args: Array[String]): Unit = {

    // #version 450 core <-- means that we will use version 4.5 of the shading language.
    // This is a single vertex, in the middle of our clip space (??? what the heck does that mean)
    // which is the coordinate system expected by the next stage of the OpenGL pipeline.
    val vertexShader = glShader.vertex(
      """
        |#version 450 core
        |void main(void)
        |{
        | gl_Position = vec4(0.0,0.0,0.5,1.0);
        |}
      """.stripMargin)

    val fragmentShader = glShader.fragment(
      """
        |#version 450 core
        |void main(void)
        |{
        | color = vec4(0.0,0.8,1.0,1.0);
        |}
      """.stripMargin)

    // (list(shaders), linked program, vertex array object)
    type State = (List[Int], Int, Int)

    object gameloop extends RenderLoopAlg[IO, State] {
      override def init(): IO[State] = for {
        _ <- IO{
          val cap = GL.createCapabilities()
          println(cap.glClearBufferfv)
        }
        vs <- vertexShader
        fs <- fragmentShader
        pg <- gl.createProgram()
        _ <- gl.attachShader(pg, vs)
        _ <- gl.attachShader(pg, fs)
        _ <- gl.linkProgram(pg)
        va <- gl.createVertexArrays()
      } yield (List(fs, vs), pg, va)

      override def render(st: State): IO[Unit] =  {
        val (_, program, vertexArray) = st
        for {
          color <- IO {
            Array(nextColor(sin[Float]), nextColor(cos[Float]), 0.0f, 1.0f)
          }
          _ <- gl.clearBufferfv(GL11.GL_COLOR, 0, color)

          _ <- gl.useProgram(program)
          _ <- gl.drawArrays(GL11.GL_POINTS, vertexArray, 1)
        } yield ()
      }

      override def terminate(st: State): IO[Unit] = {
        val (shaders, program, vertexArray) = st
        for {
          _ <- shaders.map(gl.deleteShader).sequence
          _ <- gl.deleteProgram(program)
          _ <- gl.deleteVertexArrays(vertexArray)
        } yield ()
      }
    }

    RunGlfwApp {
      for {
        w <- glfw.createOpenGL()
        _ <- glfw.renderLoop(w, gameloop)
        _ <- glfw.close(w)
      } yield ()
    }
  }

  def nextColor(f: Float => Float): Float = {
    max(f(System.nanoTime() / 1000 / 1000 / 1000), 0) * 0.5f + 0.5f
  }

}

