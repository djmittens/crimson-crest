package me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.shaders

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import me.ngrid.crimson.graphics.lwjgl.opengl.algebras.GLShaderAlg
import me.ngrid.crimson.graphics.lwjgl.opengl.algebras.GLShaderAlg.UnlinkedProgram
import org.lwjgl.opengl.{GL11, GL20}

object GL20ShaderInterpIO extends GLShaderAlg[IO, String] with LazyLogging {

/*  val GL_VERTEX_SHADER: Int = 0x8B31
  val GL_FRAGMENT_SHADER: Int = 0x8B30
  val GL_COMPILE_STATUS = 35713
  val GL_LINK_STATUS = 35714
  val GL_FALSE = 0*/

  def link(program: UnlinkedProgram): IO[Either[String, GLShaderAlg.LinkedProgram]] = IO {
    val ptr = GL20.glCreateProgram()
    logger.trace("Creating a new shader program, {}", ptr)
    program.shaders.foreach { x =>
      logger.trace("[prgm: {}] Attaching shader: {}", ptr, x)
      GL20.glAttachShader(ptr, x.ptr)
    }
    logger.trace("Linking program")
    GL20.glLinkProgram(ptr)

    if(GL20.glGetProgrami(ptr, GL20.GL_LINK_STATUS) != GL11.GL_FALSE) {
      logger.trace("Successfully linked program, {}", ptr)
      GLShaderAlg.LinkedProgram(ptr).asRight
    } else {
      val err = GL20.glGetProgramInfoLog(ptr)
      logger.trace(s"Program could not be linked: {}", err)
      GL20.glDeleteProgram(ptr)
      err.asLeft
    }
  }

  def compile(shader: GLShaderAlg.ShaderSource): IO[Either[String, GLShaderAlg.CompiledShader]] = IO {
    val tp = shader.kind match {
      case GLShaderAlg.VertexShader => GL20.GL_VERTEX_SHADER
      case GLShaderAlg.FragmentShader => GL20.GL_FRAGMENT_SHADER
    }
    val sh = GL20.glCreateShader(tp)
    logger.trace(s"creating a new shader $sh")

    //TODO: these return stuff, eg errors, need to handle them
    GL20.glShaderSource(sh, shader.source)
    GL20.glCompileShader(sh)

    if(GL20.glGetShaderi(sh, GL20.GL_COMPILE_STATUS) != GL11.GL_FALSE) {
      logger.trace(s"Successfully compiled shader $sh")
//      GLShaderAlg.Shader(sh, source, delete = IO{GL20.glDeleteShader(sh)}).asRight
      GLShaderAlg.CompiledShader(sh, shader.kind).asRight
    } else {
      val err = GL20.glGetShaderInfoLog(sh)
      logger.trace(s"Encountered an error compiling shader $sh: $err")
      GL20.glDeleteShader(sh)
      err.asLeft
    }
  }
}
