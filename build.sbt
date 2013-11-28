name := "limb-docs-searcher"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc, // "com.typesafe.play" %% "play-jdbc" % "2.2.0" withSources,
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  anorm,
  cache,
  "org.apache.commons" % "commons-io" % "1.3.2",
  "eu.henkelmann" % "actuarius_2.10.0" % "0.2.6",
  "org.pegdown" % "pegdown" % "1.0.1"
)

play.Project.playScalaSettings
