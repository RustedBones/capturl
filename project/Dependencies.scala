import sbt._

object Dependencies {

  object Versions {
    val akkaHttp   = "10.1.7"
    val javaCompat = "0.9.0"
    val parboiled  = "2.1.5"
    val scalaTest  = "3.0.5"
  }

  val akkaHttpCore = "com.typesafe.akka"      %% "akka-http-core"         % Versions.akkaHttp
  val javaCompat   = "org.scala-lang.modules" % "scala-java8-compat_2.12" % Versions.javaCompat
  val parboiled    = "org.parboiled"          %% "parboiled"              % Versions.parboiled

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  }
}
