name := "limb-docs-searcher"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  anorm,
  cache,
  "org.apache.commons" % "commons-io" % "1.3.2",
  "eu.henkelmann" % "actuarius_2.10.0" % "0.2.6",
  "org.apache.lucene" % "lucene-core" % "4.6.0",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "org.pegdown" % "pegdown" % "1.0.1"
)

play.Project.playScalaSettings
