import sbt.Keys.javacOptions

lazy val root = project.in(file("."))
  .settings(
    organization := "com.eccenca",
    name := "eccenca Json Validation engine",
    scalaVersion := "2.11.12",
    libraryDependencies += "org.glassfish" % "jakarta.json" % "1.1.5" classifier "module",
    libraryDependencies += "org.leadpony.justify" % "justify" % "1.1.0",
    libraryDependencies += "com.eccenca.di" %% "eccenca-dataintegration" % "latest.integration" % "provided->compile",
    //libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.3.4", //added this for predictable use of this lib in the script task context
    dependencyOverrides ++= Set(
      "com.google.guava" % "guava" % "18.0",
      "com.google.inject" % "guice" % "4.0",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5",
      "commons-net" % "commons-net" % "3.1",
      "com.google.code.findbugs" % "jsr305" % "3.0.0",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" // FIXME: Needs to be re-evaluated when changing the Fuseki version (currently 3.7.0), comes from jetty-servlets 9.4.7.v20170914
    ),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    scalacOptions ++= Seq("-Xlint", "-target:jvm-1.8"),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) =>
        xs map {_.toLowerCase} match {
          case "manifest.mf" :: Nil | "index.list" :: Nil | "dependencies" :: Nil =>
            MergeStrategy.discard
          case ps @ x :: xs if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
            MergeStrategy.discard
          case "plexus" :: xs =>
            MergeStrategy.discard
          case "services" :: xs =>
            MergeStrategy.filterDistinctLines
          case "spring.schemas" :: Nil | "spring.handlers" :: Nil =>
            MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.first
        }
      case _ => MergeStrategy.first
    }
  )
