ThisBuild / scalaVersion := "3.3.4"
ThisBuild / organization := "io.github.darkest"

// Publishing settings (sbt-ci-release manages version automatically)
ThisBuild / homepage := Some(url("https://github.com/darkest/anthropic-model"))
ThisBuild / licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / developers := List(
  Developer(
    id = "darkest",
    name = "Your Name",
    email = "your.email@example.com",
    url = url("https://github.com/darkest")
  )
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/darkest/anthropic-model"),
    "scm:git@github.com:darkest/anthropic-model.git"
  )
)

// Sonatype publishing
ThisBuild / sonatypeCredentialHost := "central.sonatype.com"

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
