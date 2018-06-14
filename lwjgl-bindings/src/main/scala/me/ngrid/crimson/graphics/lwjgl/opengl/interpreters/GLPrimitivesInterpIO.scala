package me.ngrid.crimson.graphics.lwjgl.opengl.interpreters

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import me.ngrid.crimson.api.graphics.PrimitivesAlg
import me.ngrid.crimson.api.graphics.PrimitivesAlg.Primitive
import me.ngrid.crimson.graphics.lwjgl.opengl.algebras.GLShaderAlg
import org.lwjgl.opengl._

object GLPrimitivesInterpIO extends LazyLogging {
  type Algebra[F[_]] = PrimitivesAlg[F, GLShaderAlg.Program[F], Primitive[F]]

  def apply(capabilities: GLCapabilities): Option[Algebra[IO]] = {
    if (capabilities.OpenGL45) {
      Gl45.some
    } else if (capabilities.OpenGL33) {
      Gl33.some
    }
    else {
      none
    }
  }

  object Gl33 extends Algebra[IO] {
    override def createPoint(shader: GLShaderAlg.Program[IO], size: Float): IO[Primitive[IO]] = ???

    override def createTriangle(shader: GLShaderAlg.Program[IO]): IO[Primitive[IO]] = {
      ???
    }
  }

  object Gl45 extends Algebra[IO] {

    override def createPoint(shader: GLShaderAlg.Program[IO], size: Float): IO[Primitive[IO]] = IO {
      val vArray = GL45.glCreateVertexArrays()
      Primitive(
        vArray,
        draw = IO {
          GL20.glUseProgram(shader.ptr)
          GL11.glPointSize(size)
          GL11.glDrawArrays(GL11.GL_POINTS, vArray, 1)
        },
        delete = IO {
          GL30.glDeleteVertexArrays(vArray)
        }
      )
    }

    override def createTriangle(shaderProgram: GLShaderAlg.Program[IO]): IO[Primitive[IO]] = IO {
      val vArray = GL45.glCreateVertexArrays()
      Primitive(
        vertexArrayPtr = vArray,
        draw = IO {
          GL30.glBindVertexArray(vArray)
          GL20.glUseProgram(shaderProgram.ptr)
          GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3)
        },
        delete = IO {
          GL30.glDeleteVertexArrays(vArray)
        }
      )
    }
  }

}
