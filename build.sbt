// General info
val username = "RustedBones"
val repo     = "capturl"

lazy val filterScalacOptions = { options: Seq[String] =>
  options.filterNot { o =>
    // get rid of value discard
    o == "-Ywarn-value-discard" || o == "-Wvalue-discard"
  }
}

/**
  * Adds a `src/.../scala-2.13+` source directory for Scala 2.13 and newer
  * and a `src/.../scala-2.13-` source directory for Scala version older than 2.13
  */
def add213CrossDirs(config: Configuration): Seq[Setting[_]] = Seq(
  unmanagedSourceDirectories in config += {
    val sourceDir = (sourceDirectory in config).value
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
      case _                       => sourceDir / "scala-2.13-"
    }
  }
)

// for sbt-github-actions
ThisBuild / crossScalaVersions := Seq("2.13.3", "2.12.12")
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(
    name = Some("Check project"),
    commands = List(
      "scalafmtCheckAll",
      "headerCheckAll",
      "capturl-contextual/scalafmtCheckAll",
      "capturl-contextual/headerCheckAll",
      "capturl-akka-http/scalafmtCheckAll",
      "capturl-akka-http/headerCheckAll"
    )
  ),
  WorkflowStep.Sbt(
    name = Some("Build project"),
    commands = List(
      "test",
      "capturl-contextual/test",
      "capturl-akka-http/test"
    )
  )
)
ThisBuild / githubWorkflowTargetBranches := Seq("master")
ThisBuild / githubWorkflowPublishTargetBranches := Seq.empty

lazy val commonSettings = add213CrossDirs(Compile) ++
  add213CrossDirs(Test) ++
  Seq(
    organization := "fr.davit",
    organizationName := "Michel Davit",
    version := "0.2.9-SNAPSHOT",
    crossScalaVersions := (ThisBuild / crossScalaVersions).value,
    scalaVersion := crossScalaVersions.value.head,
    scalacOptions ~= filterScalacOptions,
    homepage := Some(url(s"https://github.com/$username/$repo")),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    startYear := Some(2019),
    scmInfo := Some(ScmInfo(url(s"https://github.com/$username/$repo"), s"git@github.com:$username/$repo.git")),
    developers := List(
      Developer(
        id = s"$username",
        name = "Michel Davit",
        email = "michel@davit.fr",
        url = url(s"https://github.com/$username")
      )
    ),
    publishMavenStyle := true,
    Test / publishArtifact := false,
    publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
    credentials ++= (for {
      username <- sys.env.get("SONATYPE_USERNAME")
      password <- sys.env.get("SONATYPE_PASSWORD")
    } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq
  )

lazy val `capturl` = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.javaCompat,
      Dependencies.scalaCollectionCompat,
      Dependencies.parboiled,
      Dependencies.Test.scalaTest
    )
  )

lazy val `capturl-contextual` = (project in file("contextual"))
  .dependsOn(`capturl`)
  .settings(commonSettings: _*)
  .settings(
    scalaVersion := crossScalaVersions.value.last,
    libraryDependencies ++= Seq(
      Dependencies.contextual,
      Dependencies.Provided.scalaReflect(scalaVersion.value),
      Dependencies.Test.scalaTest
    )
  )

lazy val `capturl-akka-http` = (project in file("akka-http"))
  .dependsOn(`capturl`)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.akkaHttpCore,
      Dependencies.Test.akkaStream,
      Dependencies.Test.scalaTest
    )
  )
