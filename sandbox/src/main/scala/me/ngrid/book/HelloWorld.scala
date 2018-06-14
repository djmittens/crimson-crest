package me.ngrid.book

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
//import me.ngrid.crimson.client.filesystem.interpreters.TextFileInterpIO
import me.ngrid.crimson.client.graphics.lwjgl.algebras.GLShaderAlg
//import me.ngrid.crimson.client.graphics.lwjgl.algebras.{GLShader, GLShaderAlg, GLShaderProgram}
//import me.ngrid.crimson.client.graphics.lwjgl.interpreters.{GL20ShaderInterpIO, GLPrimitivesInterpIO, GlfwInterpIO}

object HelloWorld extends LazyLogging {
//  private val glfw = GlfwInterpIO
//  private val primitives = GLPrimitivesInterpIO _
//  private val basicShader = new GLShaders(GL20ShaderInterpIO)
//  private val txt = TextFileInterpIO

  def main(args: Array[String]): Unit = {
//
//    // (list(shaders), linked program, vertex array object)
//    type State = (Option[GLShaderAlg.Program[IO]], Option[PrimitivesAlg.Primitive[IO]])
//
//    object gameloop extends RenderLoopAlg[IO, State] {
//      override def init(): IO[State] = for {
//        cs <- IO {
//          GL.createCapabilities()
//        }
//        vs <- txt.readAsString("/triangleVertexShader.glsl")
//        fs <- txt.readAsString("/triangleFragmentShader.glsl")
//        pg <- basicShader.basicShaderProgram(vs, fs).map(x => x.fold(
//          s => {
//            logger.error("Failed the most basic shader program ever")
//            logger.error(s)
//            None
//          }, x =>
//            Some(x)
//        ))
//        ps <- IO(primitives(cs)).map {
//          case Left(s) =>
//            logger.error("Unable to create a primitive for rendering")
//            logger.error(s)
//            None
//          case Right(p) =>
//            Some(p)
//        }
//
//        point <- (for {
//          shader <- pg
//          primitives <- ps
//        } yield primitives.createTriangle(shader)).sequence
//
//      } yield (pg, point)
//
//      override def render(st: State): IO[Unit] = {
//        val (_, point) = st
//
//        for {
//          color <- IO {
//            Array(nextColor(System.nanoTime(), cos[Double]), 0.0f, nextColor(System.nanoTime(), sin[Double]), 0.0f, 1.0f)
//          }
//          _ <- gl.clearBufferfv(GL11.GL_COLOR, 0, color)
//          _ <- point.fold(IO.unit)(_.draw)
//          _ <- IO(Thread.sleep(200))
//        } yield ()
//      }
//
//      override def terminate(st: State): IO[Unit] = {
//        val (program, point) = st
//        for {
//          _ <- program.fold(IO.unit)(_.delete)
//          _ <- point.fold(IO.unit)(_.delete)
//        } yield ()
//      }
//    }
//
//    RunGlfwApp {
//      for {
//        w <- glfw.createOpenGL()
//        _ <- glfw.renderLoop(w, gameloop)
//        _ <- glfw.close(w)
//      } yield ()
//    }
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