package me.ngrid.crimson.graphics.lwjgl.opengl.algebras

trait GLShaderAlg[F[_], Err] {
  def link(program: UnlinkedProgram): F[Either[Err, LinkedProgram]]
  def fragment(source: CharSequence): F[Either[Err, FragmentShader]]
  def vertex(source: CharSequence): F[Either[Err, VertexShader]]

//  type MapOfShaders = F[immutable.Map[GLShaderAlg.ShaderSource, Either[Err, GLShaderAlg.CompiledShader]]]
//  def compile(sources: List[GLShaderAlg.ShaderSource])(implicit F: Monad[F]): MapOfShaders = {
//    sources.foldLeft[MapOfShaders](F.pure(immutable.Map.empty)) {(buf, shr) =>
//      buf >>= { map =>
//        compile(shr).map { x =>
//          map + (shr -> x)
//        }
//      }
//    }
//  }

  type CompiledShader

  sealed trait ShaderKind
  case class FragmentShader(value: CompiledShader) extends ShaderKind
  case class VertexShader(value: CompiledShader) extends ShaderKind

  type LinkedProgram

  case class UnlinkedProgram(
                              fragmentShader: FragmentShader,
                              vertexShader:  Option[VertexShader] = None
                            )
}
