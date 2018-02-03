package me.ngrid.crimson.client.graphics.algebras

import cats.Monad
import cats.implicits._
import me.ngrid.crimson.client.graphics.algebras.opengl.{GL11Alg, GL20Alg, GL30Alg, GL45Alg}
import me.ngrid.crimson.client.graphics.lwjgl.algebras.GLShaderProgram
import org.lwjgl.opengl.{GL11, GLCapabilities}

//TODO definitely needs to be rethought once i know more.
trait PrimitivesAlg[F[_], ShaderProgram, Point] {
  def createPoint(shader: ShaderProgram, size: Float): F[Point]
}

object GLPrimitivesInterp {
  type Algebra[F[_]] = PrimitivesAlg[F, GLShaderProgram[F], PointPrimitive[F]]

  def apply[F[_]: Monad](gl: GL11Alg[F] with GL20Alg[F] with GL30Alg[F] with GL45Alg[F])
                 (capabilities: GLCapabilities): Either[String, Algebra[F]] = {
    if (capabilities.OpenGL45) {
      new GL45(gl, gl, gl, gl).asRight[String]
    } else {
      println("Hardware fucked")
      Left("this hardware sucks")
    }
  }

  class GL45[F[_]: Monad](gl11: GL11Alg[F], gl20: GL20Alg[F], gl30: GL30Alg[F], gl45: GL45Alg[F])
    extends Algebra[F] {

    override def createPoint(shader: GLShaderProgram[F], size: Float): F[PointPrimitive[F]] = {
      for {
        vArray <- gl45.createVertexArrays()
      } yield new PointPrimitive[F](
        vArray,
        draw = for {
          _ <- gl20.useProgram(shader.ptr)
//          _ <- gl11.pointSize(size)
          _ <- gl11.drawArrays(GL11.GL_TRIANGLES, vArray, 1)
        } yield (),
        delete = gl30.deleteVertexArrays(vArray)
      )
    }
  }

  class PointPrimitive[F[_]](val vertexArrayPtr: Int, val draw: F[Unit], val delete: F[Unit])
}


sealed trait Primitive
