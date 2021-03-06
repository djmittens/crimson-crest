package me.ngrid.crimson.graphics.lwjgl.opengl.interpreters

import cats.effect.IO
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW._
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.NULL

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}
import cats.implicits._
import me.ngrid.crimson.api.graphics.{RenderLoopAlg, WindowAlg}
import org.lwjgl.opengl.GL11
//import org.lwjgl.opengl.GL

object GlfwInterpIO extends WindowAlg[IO] {
  override type Window = Long

  def init(): IO[Unit] = IO {
    // Setup an error callback. the default implementation will print the error message in System.err
    GLFWErrorCallback.createPrint(System.err).set()

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    //TODO: is there a way to read the error from glfw?
    if (!glfwInit())
      throw new IllegalStateException(" Unable to initialize GLFW")
  }

  def terminate(): IO[Unit] = IO {
    // Terminate GLFW and free the error callback
    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }

  //TODO: probably need to decompose this. a lot of stuff going on in here
  //TODO: figure out how to fit glViewport and all that jazz with this setup, eg: Callbacks!
  override def createOpenGLWindow(version: WindowAlg.OpenGLApiVersion, settings: WindowAlg.WindowSettings): IO[Window] = IO {

    // Configure GLFW
    glfwDefaultWindowHints() // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    // TODO: probably need an ADT for this, obviously would break the 4.5 examples, but we are ok for now
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, version.major)
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, version.minor)
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE)

    // Create the window
    //TODO: Add configuration for creating a window.
    val window = glfwCreateWindow(settings.width, settings.height, settings.title, NULL, NULL)
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window")

    // Setup a key callback. It will be called every time a key is pressed, repeated or released
    glfwSetKeyCallback(window, (window, key, scancode, action, mods) => {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
        glfwSetWindowShouldClose(window, true)
    })

    //TODO: this could move into a separate function, something like "center window"
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

  override def renderLoop(window: Window)(loop: RenderLoopAlg[IO]): IO[Unit] = {

    val initGlfw = IO {

      // Make the OpenGL context current
      //        glfwMakeContextCurrent(window)
      glfwMakeContextCurrent(window)

      // Enable v-sync
      glfwSwapInterval(1)
      // Make the window visible
      glfwShowWindow(window)
    }


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

    initGlfw *> loop.init().bracket(use = lp)( release = loop.terminate)
  }

  override def interceptClose(w: Window)(f: Window => Unit): IO[Unit] = ???

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
