ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

ThisBuild / organization := "com.darkest"

val tapirVersion = "1.11.11"
val circeVersion = "0.14.10"
val sttpVersion = "3.9.8"

lazy val root = (project in file("."))
  .settings(
    name := "anthropic-model",
    libraryDependencies ++= Seq(
      // Tapir
      "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % tapirVersion,

      // Sttp client
      "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
      "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,

      // Circe
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "io.circe" %% "circe-generic-extras" % "0.14.5-RC1",

      // Testing
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
