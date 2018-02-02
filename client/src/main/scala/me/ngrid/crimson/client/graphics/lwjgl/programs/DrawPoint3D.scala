package me.ngrid.crimson.client.graphics.lwjgl.programs

import me.ngrid.crimson.client.graphics.algebras.OpenGlAlg
import me.ngrid.crimson.client.graphics.programs.ContextBasedProgram
import org.lwjgl.opengl.GLCapabilities

case class DrawPoint3D[F[_]](gl: OpenGlAlg[F])
  extends ContextBasedProgram[F, GLCapabilities, DrawPoint3D.Signature[F]] {

  def drawPointGL45()
}

object DrawPoint3D {
  // Shader, color, location? TODO: add being able to add position.
  type Signature[F[_]] = (Int, (Float, Float, Float, Float)) => F[Unit]
}

