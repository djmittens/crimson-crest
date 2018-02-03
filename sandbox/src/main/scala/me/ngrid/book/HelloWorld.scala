package me.ngrid.book

import cats.Monad
import cats.effect.IO
import cats.implicits._
import me.ngrid.crimson.client.filesystem.algebra.TextFileInterpIO
import me.ngrid.crimson.client.graphics.algebras.GLPrimitivesInterp.PointPrimitive
import me.ngrid.crimson.client.graphics.algebras.{GLPrimitivesInterp, RenderLoopAlg}
import me.ngrid.crimson.client.graphics.lwjgl.RunGlfwApp
import me.ngrid.crimson.client.graphics.lwjgl.algebras.{GLShaderAlg, GLShaderProgram}
import me.ngrid.crimson.client.graphics.lwjgl.interpreters.{GlfwInterpIO, OpenGLInterpIO}
import org.lwjgl.opengl._
import spire.implicits._
import spire.math._

object HelloWorld {
  private val glfw = GlfwInterpIO
  private val gl = OpenGLInterpIO
  private val primitives = GLPrimitivesInterp(gl) _
  private val basicShader = new GLShaders(GLShaderAlg(gl))
  private val txt = TextFileInterpIO

  def main(args: Array[String]): Unit = {

    // (list(shaders), linked program, vertex array object)
    type State = (GLShaderProgram[IO], Option[PointPrimitive[IO]])

    object gameloop extends RenderLoopAlg[IO, State] {
      override def init(): IO[State] = for {
        cs <- IO{
          GL.createCapabilities()
        }
        vs <- txt.readAsString("/triangleVertexShader.glsl")
        fs <- txt.readAsString("/triangleFragmentShader.glsl")
        pg <- basicShader.basicShaderProgram(vs, fs)
        ps <- IO(primitives(cs))
        point <- ps.fold( _=> IO(None), _.createPoint(pg, 40f).map(Some.apply))
      } yield (pg, point)

      override def render(st: State): IO[Unit] =  {
        val (_, point) = st

        for {
          color <- IO {
            Array( nextColor(System.nanoTime(), cos[Double]), 0.0f,nextColor(System.nanoTime(), sin[Double]), 0.0f, 1.0f)
          }
          _ <- gl.clearBufferfv(GL11.GL_COLOR, 0, color)
          _ <- point.fold(IO.unit)(_.draw)
          _ <- IO(Thread.sleep(200))
        } yield ()
      }

      override def terminate(st: State): IO[Unit] = {
        val ( program, point) = st
        for {
          _ <- program.shaders.map(_.delete).sequence
          _ <- program.delete
          _ <- point.fold(IO.unit){ p =>
            p.delete

          }
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

  def nextColor(time: Long, f: Double => Double): Float = {
    val res = f((time / 1000).toDouble).toFloat * 0.5f + 0.5f
    res
  }

}

class GLShaders[F[_]: Monad](glShader: GLShaderAlg[F]) {
  // #version 450 core <-- means that we will use version 4.5 of the shading language.
  // This is a single vertex, in the middle of our clip space (??? what the heck does that mean)
  // which is the coordinate system expected by the next stage of the OpenGL pipeline.

  def basicShaderProgram(vertexShader: String, fragmentShader: String): F[GLShaderProgram[F]] = for {
    vs <- glShader.vertex(vertexShader)
    fs <- glShader.fragment(fragmentShader)
    pg <- glShader.createProgram(List(vs, fs))
  } yield pg
}