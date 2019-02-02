import sbt._

object Dependencies {

  object Versions {
    val scalaTest = "3.0.5"
  }

  object Test {
    val scalaTest       = "org.scalatest"     %% "scalatest"         % Versions.scalaTest % "test"
  }
}
