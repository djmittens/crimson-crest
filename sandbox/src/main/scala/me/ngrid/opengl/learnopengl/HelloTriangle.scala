package me.ngrid.opengl.learnopengl

import cats.effect.IO
import me.ngrid.crimson.api.graphics.RenderLoopAlg
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.{GL20ShaderInterpIO, GLSimpleLoop, GLViewportIO}
import me.ngrid.opengl.SimpleWindow
import org.lwjgl.opengl.{GL11, GL15, GLCapabilities}

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
    buffer <- IO {
      val vBuffer = GL15.glGenBuffers()
      GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vBuffer)
      GL15.glBufferData(vBuffer, triangleVertices, GL15.GL_STATIC_DRAW)
      vBuffer
    }
  } yield State(glVertexBuffer = buffer, glShaderProgram = 0)

  def render(state: State): IO[Unit] = IO {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT)
  }

  def terminate(state: State): IO[Unit] = IO.unit


  case class State(
                    glVertexBuffer: Int,
                    glShaderProgram: Int
                  )
  // language=GLSL
  val vertexShader: String =
    """
#version 330 core
layout (location = 0) in vec3 Pos;

void main() {
  gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
}
    """.stripMargin


  //triangle
  val triangleVertices = Array(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    0.0f, 0.5f, 0.0f
  )


  private val bg = GLViewportIO
  private val glsl = GL20ShaderInterpIO
}
