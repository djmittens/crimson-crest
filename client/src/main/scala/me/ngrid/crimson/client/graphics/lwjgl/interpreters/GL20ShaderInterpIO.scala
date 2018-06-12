package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import me.ngrid.crimson.client.graphics.lwjgl.algebras.GLShaderAlg
import org.lwjgl.opengl.GL20

object GL20ShaderInterpIO extends GLShaderAlg[IO, String] with LazyLogging {

  val GL_VERTEX_SHADER: Int = 0x8B31
  val GL_FRAGMENT_SHADER: Int = 0x8B30
  val GL_COMPILE_STATUS = 35713
  val GL_LINK_STATUS = 35714
  val GL_FALSE = 0

  def vertex(source: String): IO[Either[String, GLShaderAlg.Shader[IO]]] =
    createShader(GL_VERTEX_SHADER, source)

  def fragment(source: String): IO[Either[String, GLShaderAlg.Shader[IO]]] =
    createShader(GL_FRAGMENT_SHADER, source)

  def createShader(shaderType: Int, source: String): IO[Either[String, GLShaderAlg.Shader[IO]]] = IO {
    val sh = GL20.glCreateShader(shaderType)
    logger.trace(s"creating a new shader $sh")

    //TODO: these return stuff, eg errors, need to handle them
    GL20.glShaderSource(sh, source)
    GL20.glCompileShader(sh)

    if(GL20.glGetShaderi(sh, GL_COMPILE_STATUS) != GL_FALSE) {
      logger.trace(s"Successfully compiled shader $sh")
      GLShaderAlg.Shader(sh, source, delete = IO{GL20.glDeleteShader(sh)}).asRight
    } else {
      val err = GL20.glGetShaderInfoLog(sh)
      logger.trace(s"Encountered an error compiling shader $sh: $err")
      GL20.glDeleteShader(sh)
      err.asLeft
    }
  }

  def createShaderProgram(s: List[GLShaderAlg.Shader[IO]]): IO[Either[String, GLShaderAlg.Program[IO]]] = IO {
    val ptr = GL20.glCreateProgram()
    logger.trace("Creating a new shader program, {}", ptr)
    s.foreach { x =>
      logger.trace("[prgm: {}] Attaching shader: {}", ptr, x)
      GL20.glAttachShader(ptr, x.ptr)
    }
    logger.trace("Linking program")
    GL20.glLinkProgram(ptr)

    if(GL20.glGetProgrami(ptr, GL_LINK_STATUS) != GL_FALSE) {
      logger.trace("Successfully linked program, {}", ptr)
      GLShaderAlg.Program(ptr, s, delete = IO{GL20.glDeleteProgram(ptr)}).asRight
    } else {
      val err = GL20.glGetProgramInfoLog(ptr)
      logger.trace(s"Program could not be linked: {}", err)
      GL20.glDeleteProgram(ptr)
      err.asLeft
    }
  }
}
