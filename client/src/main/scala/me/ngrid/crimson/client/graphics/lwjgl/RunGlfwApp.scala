package me.ngrid.crimson.client.graphics.lwjgl

import cats.effect.IO
import me.ngrid.crimson.client.graphics.lwjgl.interpreters.GlfwInterpIO

object RunGlfwApp {
  def apply[T](f: => IO[T]): T = {
    GlfwInterpIO.init()
    try {
      f.unsafeRunSync()
    } finally {
      GlfwInterpIO.terminate()
    }
  }
}
