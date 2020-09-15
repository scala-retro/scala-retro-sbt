package com.github.acout.scalaretro.plugin

import sbt.Keys._
import sbt._

import java.io.{File, PrintWriter}

object ScalaRetroPlugin extends AutoPlugin {
  override val trigger: PluginTrigger = noTrigger
  override val requires: Plugins = plugins.JvmPlugin

  object autoImport extends ScalaRetroKeys
  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    configFileDir := target.value / "retro",
    retro := retroTask.value,
    retroInit := retroInitTask.value,
    // Run Scala retro after sbt doc
    //doc in Compile := (doc in Compile).dependsOn(retro).value,
    doc in Compile := Def.taskDyn {
      val result = (doc in Compile).value
      Def.task {
        val _ = retroTask.value
        result
      }
    }.value,
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

  private def retroInitTask = Def.task {
    val log = sLog.value

    val template =
      s"""# TEMPLATE FOR YAML FILE
        |- path : ${(Compile / sourceDirectory).value.getPath}
        |  outputFile : ${new File(crossTarget.value, "output.md")}
        |  tokenizer : default
        |  filters : {}
        |  display : {}
      """.stripMargin

    new PrintWriter(configFileDir.value.toString) { write(template); close() }
  }

  private def retroTask = Def.task {
    val log = sLog.value

    val config = new File(configFileDir.value.toString)

    val parsedConfig: List[DiagramConfiguration] =
      ConfigurationReader.read(config, log) match {
      case Some(c) => c
      case None => DiagramConfiguration((Compile / sourceDirectory).value.getPath,
        new File(crossTarget.value, "output.md").getPath, "default", Map.empty, Map.empty) :: Nil
    }

    log.info(s"Successfully load $config...")

    parsedConfig.foreach(dc => dc.runScalaRetro(log))
  }
}
