name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
//  cache,
  filters,
  ws,
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % Test,
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.squeryl" %% "squeryl" % "0.9.5-7"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value

sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

fork in run := true

fork in run := true
