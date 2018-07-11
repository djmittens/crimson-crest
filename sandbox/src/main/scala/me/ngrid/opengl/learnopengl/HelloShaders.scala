package me.ngrid.opengl.learnopengl

import cats.effect.IO
import me.ngrid.crimson.api.graphics.RenderLoopAlg
import me.ngrid.crimson.graphics.lwjgl.opengl.interpreters.GLSimpleLoop
import me.ngrid.opengl.SimpleWindow

object HelloShaders {
  def main(args: Array[String]): Unit = {
    SimpleWindow(
      GLSimpleLoop(
        gl => RenderLoopAlg.dynamic[IO, State] (
          _init = ???,
          _render = ???,
          _terminate = ???
        )
      )
    ).program.unsafeRunSync()
    ()
  }

  case class State()
}
