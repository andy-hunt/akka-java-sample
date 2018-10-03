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
    //autoScalaLibrary := false, // когда false, то зависимости scala не включаются если не требуются
    scalaVersion := "2.12.7",
    javacOptions := Seq("-source", "11", "-target", "11", "-Xlint"),
    libraryDependencies ++= {
      val akkaVersion = "2.5.17"
      val akkaHttpVersion = "10.1.5"
      val junitVersion = "4.12"
      Seq(
        "com.typesafe.akka" %% "akka-actor"   % akkaVersion,
        "com.typesafe.akka" %% "akka-http"  % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
        "com.typesafe.akka" %% "akka-http-jackson"    % akkaHttpVersion,

        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
        "com.novocode" % "junit-interface" % "0.11" % Test
      )
    }
  )