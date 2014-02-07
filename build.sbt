import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import sbt._
import sys.process.stringToProcess

name := "limb-docs-searcher"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.sphinx" % "sphinxapi" % "2.0.3",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "com.sun.jna" % "jna" % "3.0.9",
  "org.pegdown" % "pegdown" % "1.0.1"
)

resolvers += "Repo" at "http://evgenyg.artifactoryonline.com/evgenyg/list/libs-releases/"

play.Project.playScalaSettings
