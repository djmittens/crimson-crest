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
    program     <- createShaderProgram()
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
//    _ <- createGLVertexBuffer()
    vao <- IO {
      val vao = GL30.glGenVertexArrays()
      GL30.glBindVertexArray(vao)

      val vbo = GL15.glGenBuffers()
      GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
      GL15.glBufferData(GL15.GL_ARRAY_BUFFER, triangleVertices, GL15.GL_STATIC_DRAW)


//      val ebo = GL15.glGenBuffers()
//      GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_STATIC_DRAW)
//      GL15.glBufferData(GL15.GL_ELE)

      //The parameters for this are weird
      //TODO: review and document all the stuff thats going on in here.
      //TODO: 3*32 is a magic code for, 3 floats at a time makes a single point, but really we gotta have a better way to describe size of float
      GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * 4, 0L)
      //What the heck does this do again?
      //it enables the vertex attribute pointer that we setup (with index being 0), because when they get setup,
      // they are off by default
      GL20.glEnableVertexAttribArray(0)
      vao
    }
  } yield vao

  def render(x: State): IO[Unit] = IO {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)

    x.glShaderProgram.foreach { program =>
      GL20.glUseProgram(program.ptr)
      GL30.glBindVertexArray(x.triangleVao)
      GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
      GL30.glBindVertexArray(0)
    }
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
  final val triangleVertices: Array[Float] = Array(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    0.0f,  0.5f, 0.0f
  )

  final val rectangleVertices: Array[Float] = Array(
    0.5f,  0.5f, 0.0f,  // top right
    0.5f, -0.5f, 0.0f,  // bottom right
    -0.5f, -0.5f, 0.0f,  // bottom left
    -0.5f,  0.5f, 0.0f   // top left
  )

  final val rectangleIndices: Array[Int] = Array(  // note that we start from 0!
    0, 1, 3,   // first triangle
    1, 2, 3    // second triangle
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
}
