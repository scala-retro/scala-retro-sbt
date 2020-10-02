package com.github.acout.scalaretro.plugin.config

import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigValue}

import scala.io.Source
import com.typesafe.config.ConfigException.WrongType

import collection.JavaConverters._

object ConfigurationParser {
  type RetroConfig = Map[String, Vector[String]]

  private def read(path: String): Map[String, String] = {
    def config2map(
        co: Config,
        f: java.util.Map.Entry[String, ConfigValue] => (String, String))
      : Map[String, String] = co.entrySet().asScala.map(e => f(e)).toMap

    val content = """((?<!\")\+[a-zA-Z0-9]+)""".r
      .replaceAllIn(Source.fromFile(path).getLines.mkString("\n"), """\"$1\"""")

    val config = ConfigFactory
      .parseString(content)
      .getConfig("retro")

    val diagrams: List[ConfigObject] = try {
      config.getObjectList("diagrams").asScala.toList
    } catch {
      case _: WrongType => config.getObject("diagrams") :: Nil
    }

    config2map(config, e => e.getKey -> e.getValue.render()) + ("diagrams" -> ConfigFactory
      .parseString(
        "ids : " + diagrams.indices
          .map(i => s"D$i")
          .mkString("[", ", ", "]"))
      .getList("ids")
      .render()) ++ diagrams.zipWithIndex.flatMap {
      case (o, i) =>
        config2map(o.toConfig, e => s"D$i." + e.getKey -> e.getValue.render())
    }.toMap
  }

  private def formatConfig(config: Map[String, String]): RetroConfig = {
    val pattern = """(["']).*?\1""".r
    config.map {
      case (k, v) =>
        (k.replaceAll("\"", ""): String,
         pattern
           .findAllMatchIn(v)
           .map(_.toString.dropRight(1).drop(1))
           .toVector)
    }
  }

  private def combineGeneralWithSpecifics(
      retroConfig: RetroConfig): RetroConfig = {
    val (s, g) = (retroConfig - "diagrams").partition {
      case (k, _) => retroConfig("diagrams").contains(k.split('.').head)
    }
    val plusMinus = """[\+-]"""

    s.foldLeft(
      retroConfig("diagrams")
        .flatMap(d => g.map { case (k, v) => (s"$d.$k", v) })
        .toMap)((acc, kv) =>
      kv match {
        case (k, v)
            if k.contains("-") && acc.contains(k.replaceAll(plusMinus, "")) =>
          val key = k.replaceAll(plusMinus, "")
          acc + ((key, acc(key).diff(v)))
        case (k, v)
            if k.contains("+") && acc.contains(k.replaceAll(plusMinus, "")) =>
          val key = k.replaceAll(plusMinus, "")
          acc + ((key, acc(key).union(v)))
        case (k, v) if k.contains("-") || k.contains("+") =>
          acc + ((k.replaceAll(plusMinus, ""), v))
        case t => acc + t
    }) + (("diagrams", retroConfig("diagrams")))
  }

  private def getDiagramsConfiguration(
      retroConfig: RetroConfig): Seq[DiagramConfiguration] =
    retroConfig("diagrams").map(
      name =>
        DiagramConfiguration(
          name,
          retroConfig.getOrElse(s"$name.src", Nil).toList,
          retroConfig.get(s"$name.output").map(_.last),
          FiltersConfiguration(
            retroConfig.getOrElse(s"$name.filters.includes", Nil).toList,
            retroConfig.getOrElse(s"$name.filters.excludes", Nil).toList,
            retroConfig.getOrElse(s"$name.filters.contains", Nil).toList
          ),
          retroConfig.get(s"$name.tokenizer").map(_.last)
      ))

  def apply(
      path: String): Either[java.lang.Throwable, Seq[DiagramConfiguration]] =
    try {
      Right(
        getDiagramsConfiguration(
          combineGeneralWithSpecifics(formatConfig(read(path)))))
    } catch {
      case ex: Exception => Left(ex)
    }
}
