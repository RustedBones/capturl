import sbt._

object Dependencies {

  object Versions {
    val parboiled = "2.1.5"
    val scalaTest = "3.0.5"
  }

  val parboiled = "org.parboiled" %% "parboiled" % Versions.parboiled

  object Test {
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  }
}
