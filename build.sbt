import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import sbt._

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

debianPackageDependencies in Debian ++= Seq("default-jdk")

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/limb-docs-searcher.postinst") -> "DEBIAN/postinst"
  ) withUser "root" withGroup "root" withPerms "0755") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/limb-docs-searcher.upstart") -> "etc/init/limb-docs-searcher.conf"
  ) withUser "root" withGroup "root" withPerms "0755") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/limb-docs-searcher.default") -> "DEBIAN/default"
  ) withUser "root" withGroup "root" withPerms "0755") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/changelog") -> "DEBIAN/changelog"
  ) withUser "root" withGroup "root" withPerms "0755") asDocs()
}
