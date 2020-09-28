lazy val scala_212 = "2.12.6"

lazy val root = (project in file(".")).
  settings(
    name := "sbt-retro",
    version := "0.1",
    organization := "com.github.acout",
    scalaVersion := scala_212,
    sbtPlugin := true, 
    resolvers += Resolver.bintrayRepo("acout", "maven"),
    libraryDependencies ++= Seq(
        "com.github.acout" %% "scala-retro-core" % "0.1.4",
        "com.typesafe" % "config" % "1.4.0"
    )
  )
