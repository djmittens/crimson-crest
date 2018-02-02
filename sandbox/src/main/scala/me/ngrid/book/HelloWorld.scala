package me.ngrid.book

import cats.effect.IO
import cats.implicits._
import me.ngrid.crimson.client.graphics.algebras.GLPrimitivesInterp.PointPrimitive
import me.ngrid.crimson.client.graphics.algebras.{GLPrimitivesInterp, RenderLoopAlg}
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
  private val primitives = GLPrimitivesInterp(gl) _

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
    type State = (List[Int], Int, Option[PointPrimitive[IO]])

    object gameloop extends RenderLoopAlg[IO, State] {
      override def init(): IO[State] = for {
        cs <- IO{
          GL.createCapabilities()
        }
        vs <- vertexShader
        fs <- fragmentShader
        pg <- gl.createProgram()
        _ <- gl.attachShader(pg, vs)
        _ <- gl.attachShader(pg, fs)
        _ <- gl.linkProgram(pg)
//        va <- gl.createVertexArrays()
        ps <- IO(primitives(cs))
        point <- ps.fold( _=> IO(None), _.createPoint(pg).map(Some.apply))
      } yield (List(fs, vs), pg, point)

      override def render(st: State): IO[Unit] =  {
        val (_, _, point) = st

        for {
          color <- IO {
            Array(nextColor(sin[Double]), nextColor(cos[Double]), 0.0f, 1.0f)
          }
          _ <- gl.clearBufferfv(GL11.GL_COLOR, 0, color)

//          _ <- gl.drawArrays(GL11.GL_POINTS, vertexArray, 1)
          _ <- point.fold(IO.unit)(_.draw)
        } yield ()
      }

      override def terminate(st: State): IO[Unit] = {
        val (shaders, program, point) = st
        for {
          _ <- shaders.map(gl.deleteShader).sequence
          _ <- gl.deleteProgram(program)
//          _ <- gl.deleteVertexArrays(vertexArray)
          _ <- point.fold(IO.unit)(_.delete)
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

  def nextColor(f: Double => Double): Float = {
    val time = System.currentTimeMillis() / 1000
    max(f(time).toFloat, 0f) * 0.5f + 0.5f
  }

}

