package com.github.acout.scalaretro.plugin

import sbt.Keys._
import sbt._

import java.io.{File, PrintWriter}

import com.github.acout.scalaretro.plugin.config._

object ScalaRetroPlugin extends AutoPlugin {
  override val trigger: PluginTrigger = noTrigger
  override val requires: Plugins = plugins.JvmPlugin

  object autoImport extends ScalaRetroKeys
  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    retroConfigFile := target.value / "retro",
    retro := retroTask.value,
    retroInit := retroInitTask.value,
    // Run Scala retro after sbt doc
    retro := retro.dependsOn(compile in Compile).value,
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
      "com.typesafe" % "config" % "1.4.0"
    )
  )

  private def retroInitTask = Def.task {
    val log = sLog.value

    val template =
      s"""# TEMPLATE FOR CONFIG FILE
         |retro {
         |  diagrams : MyDiagram
         |  MyDiagram {
         |    src : "${(Compile / sourceDirectory).value.getPath.replace("\\", "/")}"
         |    output : "${new File(crossTarget.value, "output.md").getPath.replace("\\", "/")}"
         |  }
         |}
      """.stripMargin

    new PrintWriter(retroConfigFile.value.toString) { write(template); close() }
  }

  private def retroTask = Def.task {
    val log = sLog.value

    val config = retroConfigFile.value.toString

    val diagramsConfig =
      ConfigurationParser.apply(config) match {
        case Right(s) =>
          log.info(s"Successfully load $config...")
          s
        case Left(e) =>
          e.getStackTrace.foreach(st => log.error(st.toString))
          log.info(
            "Something gone wrong... Run scala retro with default parameters")
          DiagramConfiguration(
            "diagam",
            (Compile / sourceDirectory).value.getPath :: Nil,
            Some(new File(crossTarget.value, "output.md").getPath),
            FiltersConfiguration(Nil, Nil, Nil),
            Some("scala_2")
          ) :: Nil
      }

    diagramsConfig.foreach(dc => dc.generate(log))
  }
}
