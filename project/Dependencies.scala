import sbt._

object Dependencies {

  object Versions {
    val akkaHttp              = "10.2.1"
    val akka                  = "2.5.27"
    val contextual            = "2.0.0"
    val javaCompat            = "0.9.1"
    val parboiled             = "2.2.1"
    val scalaCollectionCompat = "2.2.0"
    val scalaTest             = "3.2.3"
  }

  val akkaHttpCore          = "com.typesafe.akka"      %% "akka-http-core"          % Versions.akkaHttp
  val contextual            = "com.propensive"         %% "contextual-core"         % Versions.contextual
  val javaCompat            = "org.scala-lang.modules" %% "scala-java8-compat"      % Versions.javaCompat
  val parboiled             = "org.parboiled"          %% "parboiled"               % Versions.parboiled
  val scalaCollectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % Versions.scalaCollectionCompat

  object Test {
    val scalaTest  = "org.scalatest"     %% "scalatest"   % Versions.scalaTest % "test"
    val akkaStream = "com.typesafe.akka" %% "akka-stream" % Versions.akka      % "test"
  }
}
