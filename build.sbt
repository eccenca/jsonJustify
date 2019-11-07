import sbt.Keys.{javacOptions, version}
import sbt.Process

lazy val root = project.in(file("."))
  .settings(
    organization := "com.eccenca",
    name := "eccenca Json Validation engine",
    scalaVersion := "2.11.12",
    libraryDependencies += "org.apache.johnzon" % "johnzon-core" % "1.1.12",
    libraryDependencies += "org.leadpony.justify" % "justify" % "1.1.0",
    libraryDependencies += "com.eccenca.di" %% "eccenca-dataintegration" % "latest.integration",
    libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.3.4", //added this for predictable use of this lib in the script task context
    dependencyOverrides ++= Set(
      "com.google.guava" % "guava" % "18.0",
      "com.google.inject" % "guice" % "4.0",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5",
      "commons-net" % "commons-net" % "3.1",
      "com.google.code.findbugs" % "jsr305" % "3.0.0",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" // FIXME: Needs to be re-evaluated when changing the Fuseki version (currently 3.7.0), comes from jetty-servlets 9.4.7.v20170914
    ),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    scalacOptions ++= Seq("-Xlint", "-target:jvm-1.8")
  )
