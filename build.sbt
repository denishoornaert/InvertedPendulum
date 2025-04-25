ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "de.tum"

val spinalVersion = "1.12.0"
val spinalCore = "com.github.spinalhdl" %% "spinalhdl-core" % spinalVersion
val spinalLib = "com.github.spinalhdl" %% "spinalhdl-lib" % spinalVersion
val spinalIdslPlugin = compilerPlugin("com.github.spinalhdl" %% "spinalhdl-idsl-plugin" % spinalVersion)

lazy val invertedpendulum = (project in file("."))
  .settings(
    name := "invertedpendulum", 
    Compile / scalaSource := baseDirectory.value / "hw" / "spinal",
    libraryDependencies ++= Seq(spinalCore, spinalLib, spinalIdslPlugin)
  ).dependsOn(UltraScaleSpinal)

lazy val UltraScaleSpinal = RootProject(uri("./ultrascale-spinal-wrapper/"))
fork := true

enablePlugins(ScalafmtPlugin)
