package me.ngrid.opengl.learnopengl

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import cats.syntax._
//import cats.syntax._
import me.ngrid.crimson.api.graphics.RenderLoopAlg
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.{GLSimpleLoop, GLViewportIO}
import me.ngrid.opengl.SimpleWindow
import org.lwjgl.opengl.{GL11, GLCapabilities}
//import cats.effect.implicits._
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.geometry.GLPrimitivesInterpIO
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.shaders.GL20ShaderInterpIO
//import cats.syntax._

/**
  * Implementation of Hello Triangle from learn opengl tutorial series.
  */
object HelloTriangle {

  def main(args: Array[String]): Unit = {
    SimpleWindow(GLSimpleLoop(
      gl => RenderLoopAlg.dynamic[IO, State](
        _init = init(gl),
        _render = render,
        _terminate = terminate
      )
    )).program.unsafeRunSync()
    ()
  }

  def init(gl: GLCapabilities): IO[State] = for {
    _ <- bg(gl).fold(IO.unit)(_.setBackgroundColor(0.2f, 0.3f, 0.3f, 1.0f))
    //    triangleVao <- createTriangle()
    triangle <- (for {
      program <- EitherT(createShaderProgram())
      alg <- EitherT.fromOption[IO](primitives(gl), "Primitives does not support this version of opengl")
      tri <- EitherT(alg.createRectangle(program).map(_.asRight[String]))
    } yield tri).value
  } yield State(
    triangle = triangle.toOption
  )


  private def createShaderProgram(): IO[Either[String, glsl.LinkedProgram]] = for {
    vShader <- glsl.vertex(vertexShader)
    fShader <- glsl.fragment(fragmentShader)

    k = (vShader.toValidatedNel, fShader.toValidatedNel).mapN {
      case (v, f) =>
        glsl.UnlinkedProgram(
          fragmentShader = f,
          vertexShader = Some(v)
        )
    }

    shaders: Either[String, glsl.UnlinkedProgram] = for {
      fs <- fShader
      vs <- vShader
    } yield glsl.UnlinkedProgram(
      fragmentShader = fs,
      vertexShader = Some(vs)
    )

    program <- shaders.fold(_ => IO.pure("Not all shaders exist".asLeft), x => glsl.link(x))

    _ <- fShader.fold(_ => IO.unit, glsl.delete)
    _ <- vShader.fold(_ => IO.unit, glsl.delete)
  } yield program

  def render(x: State): IO[Unit] = for {
    _ <- IO {
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
    }
    _ <- x.triangle.fold(IO.unit)(_.draw)
  } yield ()

  def terminate(x: State): IO[Unit] = {
    //todo: we can actually delete the shader objects right after we link them into a program, as we wont need them anymore
    //This is a good piece of information, because it means the program is the actual unit that matter for render calls.
    //TODO: actually delete the program as well.
    x.triangle.fold(IO.unit)(_.delete)
  }


  case class State(
    triangle: Option[GLPrimitivesInterpIO.Primitive[IO]]
  )


  // language=GLSL
  final val vertexShader: String =
    """
#version 330 core
layout (location = 0) in vec3 aPos;

void main() {
  gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
}
    """.stripMargin

  // language=GLSL
  val fragmentShader: String =
    """
#version 330 core
out vec4 FragColor;

void main()
{
    /* orangeyish color */
    FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
}
    """.stripMargin

  private val bg = GLViewportIO
  private val glsl = GL20ShaderInterpIO
  private val primitives = GLPrimitivesInterpIO
}
