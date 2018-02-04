package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.Monad
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import me.ngrid.crimson.client.graphics.algebras.opengl.GL20Alg
import me.ngrid.crimson.client.graphics.lwjgl.algebras.{GLShader, GLShaderAlg, GLShaderProgram}

case class GLShaderInterp[F[_] : Monad](gl20: GL20Alg[F])
  extends GLShaderAlg[F, String] with LazyLogging {
  val GL_VERTEX_SHADER: Int = 0x8B31
  val GL_FRAGMENT_SHADER: Int = 0x8B30
  val GL_COMPILE_STATUS = 35713
  val GL_LINK_STATUS = 35714
  val GL_FALSE = 0

  def vertex(source: String): F[Either[String, GLShader[F]]] =
    createShader(GL_VERTEX_SHADER, source)

  def fragment(source: String): F[Either[String, GLShader[F]]] =
    createShader(GL_FRAGMENT_SHADER, source)

  def createShader(shaderType: Int, source: String): F[Either[String, GLShader[F]]] = for {
    sh <- gl20.createShader(shaderType)
    _ = logger.trace(s"creating a new shader $sh")
    _ <- gl20.shaderSource(sh, source)
    _ <- gl20.compileShader(sh)
    cond <- gl20.getShaderi(sh, GL_COMPILE_STATUS)

    res <- if(cond != GL_FALSE) {
      logger.trace(s"Successfull compiled shader $sh")
      Monad[F].pure(sh.asRight[String])
    } else for {
      err <- gl20.getShaderInfoLog(sh)
      _ = logger.trace(s"Encountered an error compiling shader $err")
      _ <- gl20.deleteShader(sh)
    } yield err.asLeft[Int]
  } yield res.map(GLShader(_, source, delete = gl20.deleteShader(sh)))

  def createShaderProgram(s: List[GLShader[F]]): F[Either[String, GLShaderProgram[F]]] =
    for {
      n <- gl20.createProgram()
      _ <- Monad[F].unit.map { _ => logger.trace(s"Creating, a new shader program: $n") }
      _ <- s.map { x =>
        //FIXME: Maybe consider adding extra information about the type of shader, and maybe a name?
        logger.trace(s"Attaching a shader to the program: $n -> ${x.ptr}")
        gl20.attachShader(n, x.ptr)
      }.sequence
      _ <- gl20.linkProgram(n)
      _ = logger.trace(s"Linking program $n")

      res <- for {
        // Check if the compilation went ok.
        cond <- gl20.getProgrami(n, GL_LINK_STATUS)
        res <-
          if(cond != GL_FALSE) {
            logger.trace(s"Successfully linked program $n")
            Monad[F].pure(n.asRight[String])
          } else for {
            err <- gl20.getProgramInfoLog(n)
            _ = logger.trace(s"Program could not be linked: $err")
            _ <- gl20.deleteProgram(n)
          } yield err.asLeft[Int]
      } yield res
    } yield res.map(x => GLShaderProgram[F](
      ptr = x,
      shaders = s,
      delete = gl20.deleteProgram(n))
    )
}
