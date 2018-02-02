package me.ngrid.crimson.client.graphics.algebras

import org.lwjgl.opengl.GLCapabilities
import cats.syntax._

trait PrimitivesAlg[F[_], Shader, Location] {
  case class Point(color: Shader, location: Location)
  def newPoint(color: Shader, location: Location): F[Point]
  def drawPoint(p: Point): F[Unit]
}

object GlPrimitivesInterp {
  type Algebra[F[_]] = PrimitivesAlg[F, (Float, Float, Float, Float), (Float, Float, Float)]
  def apply[F[_]](gl: OpenGlAlg[F])(capabilities: GLCapabilities): Either[String, Algebra[F]] = {
    if(capabilities.OpenGL45) {
      (new GL45(gl)).asRight
    }
    else Left("this hardware sucks")
  }

  class GL45[F[_]](gl: OpenGlAlg[F]) {
  }
}

