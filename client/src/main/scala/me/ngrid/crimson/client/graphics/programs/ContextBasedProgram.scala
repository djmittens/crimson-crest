package me.ngrid.crimson.client.graphics.programs

trait ContextBasedProgram[F[_], Context, Program] {
  def apply(c: Context): F[Program]
}

