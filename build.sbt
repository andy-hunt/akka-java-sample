import sbt.Keys.mainClass

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "ru.liga",
  test in assembly := {}
)

lazy val app = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("ru.liga.Main"),
    assemblyJarName in assembly := "akka-java-sample.jar",
    autoScalaLibrary := false,
    scalaVersion := "2.12.4",
    javacOptions := Seq("-source", "11", "-target", "11", "-Xlint")
  )