import sbt._

object Dependencies {

  object Versions {
    val akkaHttp  = "10.1.7"
    val parboiled = "2.1.5"
    val scalaTest = "3.0.5"
  }

  val parboiled    = "org.parboiled"     %% "parboiled"      % Versions.parboiled
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % Versions.akkaHttp

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  }
}
