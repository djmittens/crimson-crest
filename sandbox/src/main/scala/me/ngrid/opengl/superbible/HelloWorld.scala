package me.ngrid.opengl.superbible

import cats.Monad
import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import me.ngrid.crimson.client.graphics.algebras.RenderLoopAlg.Aux
import me.ngrid.crimson.client.graphics.lwjgl.interpreters.{GLSimpleLoop, GlfwInterpIO}
import org.lwjgl.opengl.{GL11, GL30}
import spire.math._
import spire.implicits._
//import cats.syntax._
import com.typesafe.scalalogging.LazyLogging
import me.ngrid.crimson.client.filesystem.interpreters.TextFileInterpIO
import me.ngrid.crimson.client.graphics.algebras.{PrimitivesAlg, RenderLoopAlg}
import me.ngrid.crimson.client.graphics.lwjgl.interpreters.{GL20ShaderInterpIO, GLPrimitivesInterpIO}
import org.lwjgl.opengl.GLCapabilities
//import me.ngrid.crimson.client.filesystem.interpreters.TextFileInterpIO
import me.ngrid.crimson.client.graphics.lwjgl.algebras.GLShaderAlg
//import me.ngrid.crimson.client.graphics.lwjgl.algebras.{GLShader, GLShaderAlg, GLShaderProgram}
//import me.ngrid.crimson.client.graphics.lwjgl.interpreters.{GL20ShaderInterpIO, GLPrimitivesInterpIO, GlfwInterpIO}

object HelloWorld extends LazyLogging {
  private val glfw = GlfwInterpIO
  private val primitives = GLPrimitivesInterpIO
  private val basicShader = new GLShaders(GL20ShaderInterpIO)
  private val txt = TextFileInterpIO

  type State = Option[(GLShaderAlg.Program[IO], PrimitivesAlg.Primitive[IO])]


  def gameLoop(gl: GLCapabilities): Aux[IO, State] = RenderLoopAlg.dynamic[IO, State](
    _init = for {
      vs <- txt.readAsString("/triangleVertexShader.glsl")
      fs <- txt.readAsString("/triangleFragmentShader.glsl")
      pg <- basicShader.basicShaderProgram(vs, fs).map {
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

    _render = {
      case a @ Some((_, triangle)) =>
        IO {
          val color = Array(nextColor(System.nanoTime(), cos[Double]), 0.0f, nextColor(System.nanoTime(), sin[Double]), 0.0f, 1.0f)
          GL30.glClearBufferfv(GL11.GL_COLOR, 0, color)
        } *> triangle.draw *> IO(Thread.sleep(200)) *> IO.pure(a)

      case other => IO.pure(other)

    },
    _terminate = {
      case Some((x, y)) => x.delete *> y.delete
      case None => IO.unit
    }
  )

  def main(args: Array[String]): Unit = {

    val game = for {
      _ <- glfw.init()
      w <- glfw.createOpenGL()
      _ <- glfw.renderLoop(w, GLSimpleLoop(gameLoop) )
      _ <- glfw.close(w)
    } yield ()


    println(game.attempt.unsafeRunSync())
    ()
  }

  def nextColor(time: Long, f: Double => Double): Float = {
    val res = f((time / 1000).toDouble).toFloat * 0.5f + 0.5f
    res
  }

}

class GLShaders[F[_] : Monad, Err](glShader: GLShaderAlg[F, Err]) {
  // #version 450 core <-- means that we will use version 4.5 of the shading language.
  // This is a single vertex, in the middle of our clip space (??? what the heck does that mean)
  // which is the coordinate system expected by the next stage of the OpenGL pipeline.

  def basicShaderProgram(vertexShader: String, fragmentShader: String): F[Either[Err, GLShaderAlg.Program[F]]] = (for {
    vs <- EitherT(glShader.vertex(vertexShader))
    fs <- EitherT(glShader.fragment(fragmentShader))
    //FIXME if there are failures past this point, its possible, to leak shaders, we need to clean this
    pg <- EitherT(glShader.createShaderProgram(List(vs, fs)))
    _ <- EitherT.liftF[F, Err, Unit](deleteShaders(List(vs, fs)))
  } yield pg).value


  def deleteShaders(sh: List[GLShaderAlg.Shader[F]]): F[Unit] = {
    sh.map(x => x.delete).sequence.map(_ => ())
  }
}