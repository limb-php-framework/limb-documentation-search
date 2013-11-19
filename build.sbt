name := "limb-docs-searcher"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.apache.commons" % "commons-io" % "1.3.2",
  "eu.henkelmann" % "actuarius_2.10.0" % "0.2.6",
  "org.pegdown" % "pegdown" % "1.0.1"
)     

play.Project.playScalaSettings
