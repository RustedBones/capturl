import sbt._

object Dependencies {

  object Versions {
    val akkaHttp   = "10.2.1"
    val akka       = "2.5.27"
    val contextual = "1.2.1"
    val javaCompat = "0.9.1"
    val parboiled  = "2.2.1"
    val scalaTest  = "3.2.3"
  }

  val akkaHttpCore = "com.typesafe.akka"      %% "akka-http-core"     % Versions.akkaHttp
  val contextual   = "com.propensive"         %% "contextual"         % Versions.contextual
  val javaCompat   = "org.scala-lang.modules" %% "scala-java8-compat" % Versions.javaCompat
  val parboiled    = "org.parboiled"          %% "parboiled"          % Versions.parboiled

  object Provided {
    def scalaReflect(version: String): ModuleID = "org.scala-lang" % "scala-reflect" % version % "provided"
  }

  object Test {
    val scalaTest  = "org.scalatest"     %% "scalatest"   % Versions.scalaTest % "test"
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka      % "test"
  }
}
