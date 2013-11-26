import sbt._

name := "limb-docs-indexer"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-io" % "1.3.2",
  "eu.henkelmann" % "actuarius_2.10.0" % "0.2.6",
  "org.pegdown" % "pegdown" % "1.4.1",
  "org.streum" %% "configrity-core" % "1.0.0"
)
