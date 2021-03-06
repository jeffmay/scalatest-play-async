import Dependencies._

name := "play-test-ops-root"
scalaVersion := "2.13.5"

ThisBuild / organization := "com.rallyhealth"
ThisBuild / organizationName := "Rally Health"

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

// reload sbt when the build files change
Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / homepage := Some(url("https://github.com/play-test-ops"))
// if you contribute to this library, please add yourself to this list!
ThisBuild / developers := List(
  Developer(id = "jeffmay", name = "Jeff May", email = "jeff.n.may@gmail.com", url = url("https://github.com/jeffmay")),
)

// don't publish the jars for the root project (http://stackoverflow.com/a/8789341)
publish / skip := true

// don't search for previous artifact of the root project
mimaFailOnNoPrevious := false

def commonProject(id: String, path: String): Project = {
  Project(id, file(path)).settings(
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-deprecation:false",
      "-feature",
      "-Xfatal-warnings",
      "-Ywarn-dead-code"
    ),
    // don't publish the test code
    Test / publishArtifact := false,
    // disable compilation of ScalaDocs, since this always breaks on links
    Compile / doc / sources := Seq.empty,
    // disable publishing empty ScalaDocs
    Compile / packageDoc / publishArtifact := false
  )
}

def coreProject(includePlayVersion: String): Project = {
  val playSuffix = includePlayVersion match {
    case Play_2_5 => "25"
    case Play_2_6 => "26"
    case Play_2_7 => "27"
    case Play_2_8 => "28"
  }
  val scalaVersions = includePlayVersion match {
    case Play_2_5 => Seq(Scala_2_11)
    case Play_2_6 => Seq(Scala_2_11, Scala_2_12)
    case Play_2_7 => Seq(Scala_2_11, Scala_2_12, Scala_2_13)
    case Play_2_8 => Seq(Scala_2_12, Scala_2_13)
  }
  val path = s"play$playSuffix-core"
  commonProject(path, path).settings(
    name := s"play$playSuffix-test-ops-core",
    scalaVersion := scalaVersions.head,
    crossScalaVersions := scalaVersions,
    mimaPreviousArtifacts := Set(
      organization.value %% name.value % "1.5.0"
    ),
    // fail the build if the coverage drops below the minimum
    coverageMinimum := 80,
    coverageFailOnMinimum := true,
    // add library dependencies
    libraryDependencies ++= Seq(playServer(includePlayVersion)) ++ Seq(
      // Test-only dependencies
      playTest(includePlayVersion),
      scalaTest
    ).map(_ % Test)
  )
}

lazy val `play25-core` = coreProject(Play_2_5)
lazy val `play26-core` = coreProject(Play_2_6)
lazy val `play27-core` = coreProject(Play_2_7)
lazy val `play28-core` = coreProject(Play_2_8).settings(
  libraryDependencies ++= Seq(playTest(Play_2_8))
)
