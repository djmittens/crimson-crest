package me.ngrid.crimson.client.graphics.lwjgl.interpreters

import cats.effect.IO
import me.ngrid.crimson.client.graphics.algebras.{RenderLoopAlg, WindowAlg}
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW._
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}
import cats.implicits._

object GlfwInterpIO extends WindowAlg[IO] {
  override type Window = Long

  def init(): IO[Unit] = IO {
    // Setup an error callback. the default implementation will print the error message in System.err
    GLFWErrorCallback.createPrint(System.err).set()

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit())
      throw new IllegalStateException(" Unable to initialize GLFW")


    // Configure GLFW
    glfwDefaultWindowHints() // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
  }

  def terminate(): IO[Unit] = IO {
    // Terminate GLFW and free the error callback
    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }

  override def createOpenGL(): IO[Window] = IO {
    // Create the window
    //TODO: Add configuration for creating a window.
    val window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL)
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window")

    // Setup a key callback. It will be called every time a key is pressed, repeated or released
    glfwSetKeyCallback(window, (window, key, scancode, action, mods) => {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
        glfwSetWindowShouldClose(window, true)
    })

    // Get the thread stack and push a new frame
    tryResource(MemoryStack.stackPush()) { stack =>
      //int*
      val pWidth = stack.mallocInt(1)
      //int*
      val pHeight = stack.mallocInt(1)

      // Get the window size passed to glfwCreateWindow
      glfwGetWindowSize(window, pWidth, pHeight)

      // Get the resolution of the primary monitor

      val vidmode = glfwGetVideoMode(glfwGetMonitors().get(0))

      // Center the window
      glfwSetWindowPos(
        window,
        (vidmode.width() - pWidth.get(0)) / 2,
        (vidmode.height() - pHeight.get(0)) / 2
      )
    }.get // The stack frame is popped automatically, cause we call .close on the resource


    window
  }


  override def close(window: Window): IO[Unit] =
    IO {
      // free the window callbacks and destroy the window
      glfwFreeCallbacks(window)
      glfwDestroyWindow(window)
    }

  override def renderLoop(window: Window, loop: RenderLoopAlg[IO]): IO[Unit] = IO.pure {
    glfwMakeContextCurrent(window)

    // Enable v-sync
    glfwSwapInterval(1)
    // Make the window visible
    glfwShowWindow(window)

    // Make the OpenGL context current
    //        glfwMakeContextCurrent(window)

    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL bindings available for use.

    //    GL.createCapabilities()

    def lp(st: loop.State): IO[Unit] = IO.suspend {

      // Run the rendering loop until the user has attempted to close
      // the window or has pressed the ESCAPE key.
      if (!glfwWindowShouldClose(window))
        loop.render(st) *> IO {
          // swap the color buffers
          glfwSwapBuffers(window)
          // Poll for window events. The key callback above will only be invoked during this call.
          glfwPollEvents()
        } *> lp(st)
      else
        IO.unit
    }

    loop.init().bracket(lp)(loop.terminate)
    // TODO: figure out maybe we can just encapuslate the loop, but the recursive thing seems like its pretty good as well.

    //    val state = loop.init().unsafeRunSync()
    //    val render = loop.render(state)
    //
    //    try {
    //      // Run the rendering loop until the user has attempted to close
    //      // the window or has pressed the ESCAPE key.
    //      while (!glfwWindowShouldClose(window)) {
    //        render.unsafeRunSync()
    //
    //        // swap the color buffers
    //        glfwSwapBuffers(window)
    //
    //        // Poll for window events. The key callback above will only be invoked during this call.
    //        glfwPollEvents()
    //      }
    //    } finally {
    //      loop.terminate(state).unsafeRunSync()
    //    }
  }

  override def interceptClose(w: Window, f: Window => Unit): IO[Unit] = ???

  def tryResource[T <: AutoCloseable, U](res: => T)(f: T => U): Try[U] = {
    try {
      val r = res
      try {
        Success(f(r))
      } catch {
        case e: Throwable =>
          r.close()
          throw e
      }
    } catch {
      case NonFatal(e) => Failure(e)
      case other: Throwable => throw other
    }
  }
}
