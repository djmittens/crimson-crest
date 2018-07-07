package me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.geometry

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import me.ngrid.crimson.api.graphics.geometry.PrimitivesAlg
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.shaders.GL20ShaderInterpIO
import org.lwjgl.opengl._

object GLPrimitivesInterpIO extends LazyLogging {
  // Oh the great coupling :(  will need to redesign the whole thing later anyways.
  val glsl = GL20ShaderInterpIO

  type Algebra[F[_]] = PrimitivesAlg[F, glsl.LinkedProgram, Primitive[F]]

  case class Primitive[F[_]](vertexArrayPtr: Int, draw: F[Unit], delete: F[Unit])

  def apply(capabilities: GLCapabilities): Option[Algebra[IO]] = {
    if (capabilities.OpenGL33) {
      Gl33.some
    }
    else {
      none
    }
  }

  object Gl33 extends Algebra[IO] {
    override def createPoint(shader: glsl.LinkedProgram, size: Float): IO[Primitive[IO]] = ???

    override def createTriangle(shader: glsl.LinkedProgram): IO[Primitive[IO]] = IO {
      val vao = GL30.glGenVertexArrays()
      GL30.glBindVertexArray(vao)

      val vbo = GL15.glGenBuffers()
      GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
      GL15.glBufferData(GL15.GL_ARRAY_BUFFER, triangleVertices, GL15.GL_STATIC_DRAW)

      //The parameters for this are weird
      // TODO: review and document all the stuff thats going on in here.
      // TODO: 3*4 is a magic code for, 3 floats at a time makes a single point, but really we gotta have a better way to describe size of float
      GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * 4, 0L)
      //What the heck does this do again?
      //it enables the vertex attribute pointer that we setup (with index being 0), because when they get setup,
      // they are off by default
      GL20.glEnableVertexAttribArray(0)

      Primitive(
        vao,
        draw =  IO {
          GL20.glUseProgram(shader.ptr)
          GL30.glBindVertexArray(vao)
          GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
          GL30.glBindVertexArray(0)
          ()
        },
        delete = IO {
          ()
        }
      )
    }

    override def createRectangle(shader: glsl.LinkedProgram): IO[Primitive[IO]] = IO {
      // business as usual with setting up the vao.
      val vao = GL30.glGenVertexArrays()
      GL30.glBindVertexArray(vao)

      // Setup all of the vertices for the rectangle
      val vbo = GL15.glGenBuffers()
      GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo)
      GL15.glBufferData(GL15.GL_ARRAY_BUFFER, rectangleVertices, GL15.GL_STATIC_DRAW)

      // Setup the buffer with all the indices into the vertices array.
      val ebo = GL15.glGenBuffers()
      GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo)
      GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, rectangleIndices, GL15.GL_STATIC_DRAW)

      // Create and enable the pointer for the attributes
      GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * 4, 0L)
      GL20.glEnableVertexAttribArray(0)

      Primitive(
        vao,
        draw = IO {
          GL20.glUseProgram(shader.ptr)

          GL30.glBindVertexArray(vao)
          // why do we always draw triangles? what about a triangle fan or any other primitive type?
          GL11.glDrawElements(GL11.GL_TRIANGLES, rectangleIndices.length, GL11.GL_UNSIGNED_INT, 0)
          // not necessary but it seems like its best practice to unbind the vao at the end there
          GL30.glBindVertexArray(0)
          ()
        },
        delete = IO {
          ()
        }
      )
    }

    //triangle
    final val triangleVertices: Array[Float] = Array(
      -0.5f, -0.5f, 0.0f,
      0.5f, -0.5f, 0.0f,
      0.0f,  0.5f, 0.0f
    )

    final val rectangleVertices: Array[Float] = Array(
      0.5f, 0.5f, 0.0f, // top right
      0.5f, -0.5f, 0.0f, // bottom right
      -0.5f, -0.5f, 0.0f, // bottom left
      -0.5f, 0.5f, 0.0f // top left
    )

    final val rectangleIndices: Array[Int] = Array( // note that we start from 0!
      0, 1, 3, // first triangle
      1, 2, 3 // second triangle
    )
  }

}
