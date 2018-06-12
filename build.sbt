
inThisBuild(Seq(
  scalaOrganization := "org.typelevel",
  scalaVersion := "2.12.4-bin-typelevel-4"
))

lazy val `crimson-crest` = (project in file(".")).aggregate(client, sandbox)

lazy val client = (project in file("client")).
  enablePlugins(ProjectPlugin).
  settings(
    //    crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.4", "2.13.0-M2"),
    libraryDependencies ++=
      libgdx ++ cats ++ logging
  )

lazy val `fbx-format` = (project in file("fbx-format")).
  enablePlugins(ProjectPlugin).
  settings(
    libraryDependencies ++=
      libgdx ++ cats ++ logging
  )

lazy val sandbox = (project in file("sandbox")).
  enablePlugins(ProjectPlugin).dependsOn(client)

val lwjglVersion = "3.1.5"

val libgdx = Seq(
  "org.lwjgl" % "lwjgl" % lwjglVersion,
  "org.lwjgl" % "lwjgl-assimp" % lwjglVersion,
  "org.lwjgl" % "lwjgl-bgfx" % lwjglVersion,
  "org.lwjgl" % "lwjgl-egl" % lwjglVersion,
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion,
  "org.lwjgl" % "lwjgl-jawt" % lwjglVersion,
  "org.lwjgl" % "lwjgl-jemalloc" % lwjglVersion,
  "org.lwjgl" % "lwjgl-lmdb" % lwjglVersion,
  "org.lwjgl" % "lwjgl-lz4" % lwjglVersion,
  "org.lwjgl" % "lwjgl-nanovg" % lwjglVersion,
  "org.lwjgl" % "lwjgl-nfd" % lwjglVersion,
  "org.lwjgl" % "lwjgl-nuklear" % lwjglVersion,
  "org.lwjgl" % "lwjgl-odbc" % lwjglVersion,
  "org.lwjgl" % "lwjgl-openal" % lwjglVersion,
  "org.lwjgl" % "lwjgl-opencl" % lwjglVersion,
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion,
  "org.lwjgl" % "lwjgl-opengles" % lwjglVersion,
  "org.lwjgl" % "lwjgl-openvr" % lwjglVersion,
  "org.lwjgl" % "lwjgl-ovr" % lwjglVersion,
  "org.lwjgl" % "lwjgl-par" % lwjglVersion,
  "org.lwjgl" % "lwjgl-remotery" % lwjglVersion,
  "org.lwjgl" % "lwjgl-rpmalloc" % lwjglVersion,
  "org.lwjgl" % "lwjgl-sse" % lwjglVersion,
  "org.lwjgl" % "lwjgl-stb" % lwjglVersion,
  "org.lwjgl" % "lwjgl-tinyexr" % lwjglVersion,
  "org.lwjgl" % "lwjgl-tinyfd" % lwjglVersion,
  "org.lwjgl" % "lwjgl-tootle" % lwjglVersion,
  "org.lwjgl" % "lwjgl-vulkan" % lwjglVersion,
  "org.lwjgl" % "lwjgl-xxhash" % lwjglVersion,
  "org.lwjgl" % "lwjgl-yoga" % lwjglVersion,
  "org.lwjgl" % "lwjgl-zstd" % lwjglVersion,
) ++
  Seq("natives-windows", "natives-macos", "natives-linux").
    flatMap(lwjglNatives(lwjglVersion, _))

def lwjglNatives(lwjglVersion: String, lwjglNatives: String) = Seq(
  "org.lwjgl" % "lwjgl" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-assimp" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-bgfx" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-glfw" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-jemalloc" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-lmdb" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-lz4" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-nanovg" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-nfd" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-nuklear" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-openal" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-opengl" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-opengles" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-openvr" % lwjglVersion classifier lwjglNatives,
  //  "org.lwjgl" % "lwjgl-ovr" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-par" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-remotery" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-rpmalloc" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-sse" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-stb" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-tinyexr" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-tinyfd" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-tootle" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-xxhash" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-yoga" % lwjglVersion classifier lwjglNatives,
  "org.lwjgl" % "lwjgl-zstd" % lwjglVersion classifier lwjglNatives,
)

val catsVersion = "1.0.1"

val cats = Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-kernel" % catsVersion,
  "org.typelevel" %% "cats-macros" % catsVersion,
  "org.typelevel" %% "cats-effect" % "0.8",
  "org.typelevel" %% "mouse" % "0.16",

  //  "org.typelevel" %% "cats-mtl" % catsVersion

  //MATH
  "org.typelevel" %% "spire" % "0.14.1",
)

val logging = Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
)