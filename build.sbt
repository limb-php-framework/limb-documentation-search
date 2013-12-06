import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._

name := "limb-docs-searcher"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  anorm,
  cache,
  "org.sphinx" % "sphinxapi" % "2.0.3",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "eu.henkelmann" % "actuarius_2.10.0" % "0.2.6",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "org.pegdown" % "pegdown" % "1.0.1"
)

resolvers += "Repo" at "http://evgenyg.artifactoryonline.com/evgenyg/list/libs-releases/"

play.Project.playScalaSettings

description in Linux := "The description"

packageDescription in Linux := "The description"

packageSummary := "A package summary"

maintainer := "Andrew Rezcov <rsa199@ya.ru>"
