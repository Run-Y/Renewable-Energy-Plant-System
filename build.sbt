ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "REPS"
  )

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client3" %% "core" % "3.9.0",
  "com.softwaremill.sttp.client3" %% "play-json" % "3.9.0",
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.9.0",
  "com.typesafe.play" %% "play-json" % "2.9.4"
)
