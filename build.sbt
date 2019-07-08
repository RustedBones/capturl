// General info
val username = "RustedBones"
val repo     = "capturl"

lazy val commonSettings = Seq(
  organization := "fr.davit",
  version := "0.1.1-SNAPSHOT",
  // disable cross build. project does not compile yet on scala 2.11 and 2.13
  // crossScalaVersions := Seq("2.12.8", "2.13.0"),
  scalaVersion := "2.12.8", // crossScalaVersions.value.last,
  Compile / compile / scalacOptions ++= Settings.scalacOptions(scalaVersion.value),
  homepage := Some(url(s"https://github.com/$username/$repo")),
  licenses += "APACHE" -> url(s"https://github.com/$username/$repo/blob/master/LICENSE"),
  scmInfo := Some(ScmInfo(url(s"https://github.com/$username/$repo"), s"git@github.com:$username/$repo.git")),
  developers := List(
    Developer(
      id = s"$username",
      name = "Michel Davit",
      email = "michel@davit.fr",
      url = url(s"https://github.com/$username"))
  ),
  publishMavenStyle := true,
  Test / publishArtifact := false,
  publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
  credentials ++= (for {
    username <- sys.env.get("SONATYPE_USERNAME")
    password <- sys.env.get("SONATYPE_PASSWORD")
  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq,
)

lazy val `capturl` = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.contextual,
      Dependencies.javaCompat,
      Dependencies.parboiled,
      Dependencies.Test.scalaTest
    )
  )

lazy val `capturl-akka-http` = (project in file("akka-http"))
  .dependsOn(`capturl`)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.akkaHttpCore,
      Dependencies.Test.scalaTest
    )
  )
