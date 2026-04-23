ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "JG1_AlexandraCardoso129865_JoanaCardoso129867_RuiFernandes129857"
  )

libraryDependencies += "org.openjfx" % "javafx-base" % "25.0.2"
libraryDependencies += "org.openjfx" % "javafx-controls" % "25.0.2"
libraryDependencies += "org.openjfx" % "javafx-fxml" % "25.0.2"