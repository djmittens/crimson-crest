package me.ngrid

import org.lwjgl._
import org.lwjgl.glfw.Callbacks._
import org.lwjgl.glfw.GLFW._
import org.lwjgl.glfw._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl._
import org.lwjgl.system.MemoryUtil._
import org.lwjgl.system._

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object LwjglHelloWorld {
  var window: Long = _

  def main(args: Array[String]): Unit = {
    run()
  }

  def run(): Unit = {
    println(s"Hello LWJGL ${Version.getVersion}!")

    init()
    loop()

    // free the window callbacks and destroy the window
    glfwFreeCallbacks(window)
    glfwDestroyWindow(window)

    // Terminate GLFW and free the error callback
    glfwTerminate()
    glfwSetErrorCallback(null).free()
  }

  def init(): Unit = {
    // Setup an error callback. the default implementation will print the error message in System.err
    GLFWErrorCallback.createPrint(System.err).set()

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit())
      throw new IllegalStateException(" Unable to initialize GLFW")

    // Configure GLFW
    glfwDefaultWindowHints() // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    // Create the window
    window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL)
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window")

    // Setup a key callback. It will be called every time a key is pressed, repeated or released
    glfwSetKeyCallback(window, (window, key, scancode, action, mods) => {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
        glfwSetWindowShouldClose(window, true)
    })

    // Get the thread stack and push a new frame
    tryResource(MemoryStack.stackPush()) { stack =>
      val pWidth = stack.mallocInt(1) // int*
    val pHeight = stack.mallocInt(1) // int*

      // Get the window size passed to glfwCreateWindow
      glfwGetWindowSize(window, pWidth, pHeight)

      // Get the resolution of the primary monitor
      val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

      // Center the window
      glfwSetWindowPos(
        window,
        (vidmode.width() - pWidth.get(0)) / 2,
        (vidmode.width() - pHeight.get(0)) / 2
      )
    } // The stack rame is popped automatically, cause we call .close on the resource

    // Make the OpenGL context current
    glfwMakeContextCurrent(window)
    // Enable v-sync
    glfwSwapInterval(1)

    // Mak ethe window visible
    glfwShowWindow(window)
  }

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

  def loop(): Unit = {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the GLCapabilities instance and makes the OpenGL bindings available for use.

    GL.createCapabilities()

    // Set the clear color
    glClearColor(1.0f, 0.0f, 0.0f, 0.0f)

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while(!glfwWindowShouldClose(window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT) // clear the framebuffer
      glfwSwapBuffers(window) // swap the color buffers

      // Poll for window events. The key callback above will only be invoked during this call.
      glfwPollEvents()
    }
  }
}
