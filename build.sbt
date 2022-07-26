import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "ru.otus"
ThisBuild / organizationName := "otus"

lazy val root = (project in file("."))
  .settings(
    name := "scala-dev-mooc-2022-03",
    libraryDependencies ++= Dependencies.zio,
    libraryDependencies ++= Dependencies.pureconfig,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += catsCore,
    libraryDependencies ++= catsEffect,
    libraryDependencies ++= fs2,
    libraryDependencies ++= http4s,
    addCompilerPlugin(Dependencies.kindProjector)
  )

scalacOptions += "-Ymacro-annotations"
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
