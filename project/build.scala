import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.earldouglas.xwp.JettyPlugin

object RiksdagskollenBuild extends Build {
  val Organization = "se.riksdagskollen"
  val Name = "Riksdagskollen"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.8"
  val ScalatraVersion = "2.4.1"

  lazy val project = Project (
    "Riksdagskollen",
    file("."),
    settings = ScalatraPlugin.scalatraSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-json" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % Test,
        "org.json4s"   %% "json4s-jackson" % "3.3.0",
        "mysql" % "mysql-connector-java" % "5.1.39",
        "ch.qos.logback" % "logback-classic" % "1.1.7" % Runtime,
        "javax.servlet" % "javax.servlet-api" % "3.1.0",
        "org.eclipse.jetty" % "jetty-webapp" % "9.3.9.v20160517" % "container;compile"
      )
    )
  ).enablePlugins(JettyPlugin)
}
