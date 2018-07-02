package me.ngrid.crimson.graphics.lwjgl.opengl.algebras

import cats.Monad
import cats.implicits._
import me.ngrid.crimson.graphics.lwjgl.opengl.algebras.GLShaderAlg._

import scala.collection.immutable

trait GLShaderAlg[F[_], Err] {
  def link(program: UnlinkedProgram): F[Either[Err, LinkedProgram]]
  def fragment(source: CharSequence): F[Either[Err, FragmentShader]]

  type MapOfShaders = F[immutable.Map[GLShaderAlg.ShaderSource, Either[Err, GLShaderAlg.CompiledShader]]]
  def compile(sources: List[GLShaderAlg.ShaderSource])(implicit F: Monad[F]): MapOfShaders = {
    sources.foldLeft[MapOfShaders](F.pure(immutable.Map.empty)) {(buf, shr) =>
      buf >>= { map =>
        compile(shr).map { x =>
          map + (shr -> x)
        }
      }
    }
  }

  type FragmentShader
  type VertexShader

  type LinkedProgram

  case class UnlinkedProgram(
                              fragmentShader: FragmentShader,
                              vertexShader:  Option[VertexShader] = None
                            )
}
