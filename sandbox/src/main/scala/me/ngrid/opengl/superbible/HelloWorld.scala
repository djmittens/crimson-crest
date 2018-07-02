package me.ngrid.opengl.superbible

import cats.Monad
import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import me.ngrid.crimson.api.graphics.{RenderLoopAlg, WindowAlg}
import me.ngrid.crimson.assets.TextFileInterpIO
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.geometry.GLPrimitivesInterpIO
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.geometry.GLPrimitivesInterpIO.Primitive
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.shaders.GL20ShaderInterpIO
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.{GLSimpleLoop, GlfwInterpIO}
import org.lwjgl.opengl.{GL11, GL30}
import spire.implicits._
import spire.math._
//import cats.syntax._
import com.typesafe.scalalogging.LazyLogging
import org.lwjgl.opengl.GLCapabilities
//import me.ngrid.crimson.api.filesystem.interpreters.TextFileInterpIO
//import me.ngrid.crimson.graphics.lwjgl.opengl.algebras.{GLShader, GLShaderAlg, GLShaderProgram}
//import me.ngrid.crimson.goraphics.lwjgl.opengl.interpreters.{GL20ShaderInterpIO, GLPrimitivesInterpIO, GlfwInterpIO}

object HelloWorld extends LazyLogging {
  private val glfw = GlfwInterpIO
  private val primitives = GLPrimitivesInterpIO
  private val glsl = GL20ShaderInterpIO
  private val txt = TextFileInterpIO

  private type State = Option[(glsl.LinkedProgram, Primitive[IO])]

  private def gameLoop(gl: GLCapabilities): RenderLoopAlg.Aux[IO, State] = RenderLoopAlg.dynamic[IO, State](
    _init = for {
      vs <- txt.readAsString("/triangleVertexShader.glsl")
      fs <- txt.readAsString("/triangleFragmentShader.glsl")
      pg <- GLShaders.basicShaderProgram(vs, fs).map {
        case Left(e) =>
          logger.error("Failed to compile the most basic shader program ever\n {}", e)
          None
        case Right(x) =>
          Some(x)
      }

      triangle <- (for {
        pg <- pg
        ps <- primitives(gl)
      } yield ps.createTriangle(pg)).sequence

    } yield pg.product(triangle),

    _render = x => IO {
      val color = Array(nextColor(System.nanoTime(), cos[Double]), 0.0f, nextColor(System.nanoTime(), sin[Double]), 0.0f, 1.0f)
      GL30.glClearBufferfv(GL11.GL_COLOR, 0, color)
    } *> (x match {
      case Some((_, triangle)) =>
        triangle.draw *> IO(Thread.sleep(200))

      case _ => IO.unit
    }),
    _terminate = {
      case Some(_) =>
        IO.unit
//        x.delete *> y.delete
      case None => IO.unit
    }
  )

  def main(args: Array[String]): Unit = {

    val game = for {
      _ <- glfw.init()
      w <- glfw.createOpenGLWindow(WindowAlg.GL33, WindowAlg.WindowSettings(
        height = 300,
        width = 400,
        title = "Hello World !!!"
      ))
      _ <- glfw.renderLoop(w)(GLSimpleLoop(gameLoop))
      _ <- glfw.close(w)
    } yield ()


    println(game.attempt.unsafeRunSync())
    ()
  }

  def nextColor(time: Long, f: Double => Double): Float = {
    val res = f((time / 1000).toDouble).toFloat * 0.5f + 0.5f
    res
  }

  private object GLShaders {
    // #version 450 core <-- means that we will use version 4.5 of the shading language.
    // This is a single vertex, in the middle of our clip space (??? what the heck does that mean)
    // which is the coordinate system expected by the next stage of the OpenGL pipeline.

    def basicShaderProgram(vertexShader: String, fragmentShader: String): IO[Either[String, glsl.LinkedProgram]] = (for {
      //    vs <- EitherT(glShader.compile(GLShaderAlg.ShaderSource(vertexShader, GLShaderAlg.VertexShader)))
      //    fs <- EitherT(glShader.compile(GLShaderAlg.ShaderSource(fragmentShader, GLShaderAlg.FragmentShader)))
      vs <- EitherT(glsl.vertex(vertexShader))
      fs <-EitherT(glsl.fragment(fragmentShader))
      //FIXME if there are failures past this point, its possible, to leak shaders, we need to clean this
      pg <- EitherT(glsl.link(glsl.UnlinkedProgram(
        fragmentShader = fs,
        vertexShader = Some(vs)
      )))
      //TODO: actually delete some shaders
      //    _ <- EitherT.liftF[F, Err, Unit](deleteShaders(List(vs, fs)))
    } yield pg).value


    def deleteShaders(@deprecated("unused", "") x: List[glsl.CompiledShader]): IO[Unit] = {
      Monad[IO].unit
    }
  }
}

