package me.ngrid.opengl.learnopengl

import cats.effect.IO
import me.ngrid.crimson.api.graphics.RenderLoopAlg
import me.ngrid.crimson.graphics.lwjgl.opengl.algebras.GLShaderAlg
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.{GL20ShaderInterpIO, GLSimpleLoop, GLViewportIO}
import me.ngrid.opengl.SimpleWindow
import org.lwjgl.opengl.{GL11, GL15, GL20, GL30, GLCapabilities}
import cats.implicits._
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
    triangleVao <- createTriangle()
    program <- createShaderProgram()
  } yield State(
    triangleVao = triangleVao,
    glShaderProgram = program
  )

  def createShaderProgram(): IO[Either[String, GLShaderAlg.Program[IO]]] = for {
    fShader <- glsl.createShader(glsl.GL_FRAGMENT_SHADER, fragmentShader)
    vShader <- glsl.createShader(glsl.GL_VERTEX_SHADER, vertexShader)
    shaders = List(fShader, vShader)
    //in intellij this is red, (why why intellij?), but it actually compiles!!
    program <- shaders.sequence.map(glsl.createShaderProgram).sequence
    _ <- shaders.traverse {
      case Left(_) => IO.unit
      case Right(x) => x.delete
    }
  } yield program.flatten

  def createTriangle(): IO[Int] = for {
    vao <- createGLVertexArrayObject()
    _ <- createGLVertexBuffer()
    _ <- IO {
      //The parameters for this are weird
      //TODO: review and document all the stuff thats going on in here.
      //TODO: 3*32 is a magic code for, 3 floats at a time makes a single point, but really we gotta have a better way to describe size of float
      GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * 32, 0L)
      //What the heck does this do again?
      GL20.glEnableVertexAttribArray(0)
    }
  } yield vao

  def createGLVertexArrayObject(): IO[Int] = IO {
    val vao = GL30.glGenVertexArrays()
    GL30.glBindVertexArray(vao)
    vao
  }

  def createGLVertexBuffer(): IO[Int] = IO {
    val vBuffer = GL15.glGenBuffers()
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vBuffer)
    GL15.glBufferData(vBuffer, triangleVertices, GL15.GL_STATIC_DRAW)
    vBuffer
  }


  def render(x: State): IO[Unit] = IO {
    x.glShaderProgram.foreach { program =>
      GL20.glUseProgram(program.ptr)
      GL30.glBindVertexArray(x.triangleVao)
      GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
    }


    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
  }

  def terminate(x: State): IO[Unit] = {
    //todo: we can actually delete the shader objects right after we link them into a program, as we wont need them anymore
    //This is a good piece of information, because it means the program is the actual unit that matter for render calls.
    x.glShaderProgram.traverse(_.delete) *> IO.unit
  }


  case class State(
                    triangleVao: Int,
                    glShaderProgram: Either[String, GLShaderAlg.Program[IO]]
                  )


  //triangle
  val triangleVertices = Array(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    0.0f, 0.5f, 0.0f
  )

  // language=GLSL
  val vertexShader: String =
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
}
