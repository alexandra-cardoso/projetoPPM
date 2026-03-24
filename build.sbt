ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.2"

libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-colections" % "1.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "projeto"
  )
