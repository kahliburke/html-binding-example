import sbt.CrossVersion

enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "html-binding-example"

version := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.10"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

scalacOptions ++= Seq("-Xxml:coalescing", "-P:scalajs:sjsDefinedByDefault")//, "-Ymacro-debug-lite")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.7",
  "org.lrng.binding" %%% "html" % "1.0.2",
  "com.thoughtworks.binding" %%% "futurebinding" % "11.8.1",
)

//scalaJSLinkerConfig ~= { _.withESFeatures(_.withUseECMAScript2015(true)) }