package me.ngrid.crimson.client.graphics.lwjgl.algebras

import cats.Monad
import cats.implicits._
import me.ngrid.crimson.client.graphics.algebras.opengl.GL20Alg

case class GLShaderAlg[F[_] : Monad](gl20: GL20Alg[F]) {
  val GL_VERTEX_SHADER: Int = 0x8B31
  val GL_FRAGMENT_SHADER: Int = 0x8B30

  def vertex(source: String): F[GLShader[F]] = createShader(GL_VERTEX_SHADER, source)

  def fragment(source: String): F[GLShader[F]] = createShader(GL_FRAGMENT_SHADER, source)

  def createShader(shaderType: Int, source: String): F[GLShader[F]] = for {
    sh <- gl20.createShader(shaderType)
    _ <- gl20.shaderSource(sh, source)
  } yield GLShader(sh, source, compile = gl20.compileShader(sh), delete = gl20.deleteShader(sh))

  def createProgram(s: List[GLShader[F]]): F[GLShaderProgram[F]] =
    gl20.createProgram().map { n =>
      GLShaderProgram[F](
        ptr = n,
        shaders = s,
        link = for {
          _ <- s.map(x => gl20.attachShader(n, x.ptr)).sequence
          _ <- gl20.linkProgram(n)
        } yield (),
        delete = for {
          _ <- gl20.deleteProgram(n)
        } yield ()
      )
    }
}

case class GLShader[F[_]](ptr: Int, source: String, compile: F[Unit], delete: F[Unit])

case class GLShaderProgram[F[_]](ptr: Int, shaders: List[GLShader[F]], link: F[Unit], delete: F[Unit])
