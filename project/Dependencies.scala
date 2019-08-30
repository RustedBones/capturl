import sbt._

object Dependencies {

  object Versions {
    val akkaHttp   = "10.1.8"
    val contextual = "1.2.1"
    val javaCompat = "0.9.0"
    val parboiled  = "2.1.8"
    val scalaTest  = "3.0.8"
  }

  def scalaReflect(version: String): ModuleID = "org.scala-lang" % "scala-reflect" % version

  val akkaHttpCore = "com.typesafe.akka"      %% "akka-http-core"     % Versions.akkaHttp
  val contextual   = "com.propensive"         %% "contextual"         % Versions.contextual
  val javaCompat   = "org.scala-lang.modules" %% "scala-java8-compat" % Versions.javaCompat
  val parboiled    = "org.parboiled"          %% "parboiled"          % Versions.parboiled

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  }
}
