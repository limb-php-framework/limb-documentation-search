import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import sbt._

name := "limb-docs-indexer"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.pegdown" % "pegdown" % "1.4.1",
  "org.streum" %% "configrity-core" % "1.0.0"
)

packageArchetype.java_application

description in Debian := "The description"

packageDescription in Debian := "The description"

maintainer := "Andrew Rezcov <rsa199@ya.ru>"

debianPackageDependencies in Debian ++= Seq("default-jdk")

debianPackageRecommends in Debian += "scala"

debianPackageRecommends in Debian += "sbt"

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/changelog") -> "DEBIAN/changelog"
  ) withUser "root" withGroup "root" withPerms "0644") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "conf/config") -> "/etc/limb-docs-indexer/config"
  ) withUser "root" withGroup "root" withPerms "0644") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/limb-docs-indexer.postinst") -> "DEBIAN/postinst"
  ) withUser "root" withGroup "root" withPerms "0775") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/limb-docs-indexer.cron.d") -> "etc/cron.d/limb-docs-indexer"
  ) withUser "root" withGroup "root" withPerms "0775") asDocs()
}

linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
  (packageMapping(
    (bd / "debian/sphinx.conf") -> "etc/limb-docs-indexer/sphinx.conf"
  ) withUser "root" withGroup "root" withPerms "0775") asDocs()
}
