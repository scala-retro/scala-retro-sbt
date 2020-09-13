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
        // https://mvnrepository.com/artifact/org.yaml/snakeyaml
        "org.yaml" % "snakeyaml" % "1.26",
        "io.circe" %% "circe-parser" % "0.12.0",
        "io.circe" %% "circe-generic" % "0.12.0",
        "io.circe" %% "circe-yaml" % "0.12.0"
    )
  )
