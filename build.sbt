import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import sbt._
import sys.process.stringToProcess

name := "limb-docs-searcher"

version := ("dpkg-parsechangelog" !!).split("\n")(1).replace("Version: ", "") + "-all"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.sphinx" % "sphinxapi" % "2.0.3",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "eu.henkelmann" % "actuarius_2.10.0" % "0.2.6",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "org.pegdown" % "pegdown" % "1.0.1",
  "org.streum" %% "configrity-core" % "1.0.0"
)

resolvers += "Repo" at "http://evgenyg.artifactoryonline.com/evgenyg/list/libs-releases/"

play.Project.playScalaSettings

description in Linux := "Limb documents searcher"

packageDescription in Linux := "Limb documents searcher"

packageSummary := "Limb documents searcher"

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
    (bd / "debian/limb-docs-searcher.default") -> "etc/default/limb-docs-searcher"
  ) withUser "root" withGroup "root" withPerms "0755") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/limb-docs-searcher.cron.d") -> "etc/cron.d/limb-docs-searcher"
  ) withUser "root" withGroup "root" withPerms "0755") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/changelog") -> "DEBIAN/changelog"
  ) withUser "root" withGroup "root" withPerms "0755") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/sphinx.conf") -> "etc/limb-docs-indexer/sphinx.conf"
  ) withUser "root" withGroup "root" withPerms "0775") asDocs()
}
