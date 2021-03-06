package me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.shaders

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import me.ngrid.crimson.graphics.lwjgl.opengl.algebras.GLShaderAlg
import org.lwjgl.opengl.{GL11, GL20}

object GL20ShaderInterpIO extends GLShaderAlg[IO, String] with LazyLogging {

  def link(program: UnlinkedProgram): IO[Either[String, LinkedProgram]] = IO {
    val ptr = GL20.glCreateProgram()
    logger.trace("Creating a new shader program, {}", ptr)

    def attachShader(shr: Int): Unit = {
      logger.trace("[prgm: {}] Attaching shader: {}", ptr, shr)
      GL20.glAttachShader(ptr, shr)
    }

    // Actually attach all of the shaders
    attachShader(program.fragmentShader.ptr)
    program.vertexShader.foreach(x => attachShader(x.ptr))

    logger.trace("Linking program")
    GL20.glLinkProgram(ptr)

    if(GL20.glGetProgrami(ptr, GL20.GL_LINK_STATUS) != GL11.GL_FALSE) {
      logger.trace("Successfully linked program, {}", ptr)
      LinkedProgram(ptr).asRight
    } else {
      val err = GL20.glGetProgramInfoLog(ptr)
      logger.trace(s"Program could not be linked: {}", err)
      GL20.glDeleteProgram(ptr)
      err.asLeft
    }
  }

  override def fragment(source: CharSequence): IO[Either[String, CompiledShader[Shader.Fragment.type]]] =
    compile(source, Shader.Fragment)

  override def vertex(source: CharSequence): IO[Either[String, CompiledShader[Shader.Vertex.type]]] =
    compile(source, Shader.Vertex)

  private[this] def compile[T <: Shader](src: CharSequence, kind: T): IO[Either[String, CompiledShader[T]]] = IO {
    val iKind = kind match {
      case Shader.Vertex => GL20.GL_VERTEX_SHADER
      case Shader.Fragment => GL20.GL_FRAGMENT_SHADER
      case _ => -1

    }

    val sh = GL20.glCreateShader(iKind)
    logger.trace(s"creating a new shader $sh")

    //TODO: these return stuff, eg errors, need to handle them
    GL20.glShaderSource(sh, src)
    GL20.glCompileShader(sh)

    if(GL20.glGetShaderi(sh, GL20.GL_COMPILE_STATUS) != GL11.GL_FALSE) {
      logger.trace(s"Successfully compiled shader $sh")
      //      GLShaderAlg.Shader(sh, source, delete = IO{GL20.glDeleteShader(sh)}).asRight
//      GLShaderAlg.CompiledShader(sh, shader.kind).asRight

      CompiledShader(sh, kind).asRight
    } else {
      val err = GL20.glGetShaderInfoLog(sh)
      logger.trace(s"Encountered an error compiling shader $sh: $err")
      GL20.glDeleteShader(sh)
      err.asLeft
    }
  }


  override def delete(shader: GL20ShaderInterpIO.CompiledShader[_]): IO[Unit] = IO {
    logger.trace("Deleting glsl shader {}", shader)
    GL20.glDeleteShader(shader.ptr)
    ()
  }

  override def delete(program: GL20ShaderInterpIO.LinkedProgram): IO[Unit] = IO {
    logger.trace("Deleting glsl program {}", program)
    GL20.glDeleteProgram(program.ptr)
  }

  type Pointer = Int
}
