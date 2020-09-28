package com.github.acout.scalaretro.plugin.config

import com.typesafe.config.ConfigFactory

import scala.io.Source
import collection.JavaConverters._

object ConfigurationParser {
  type RetroConfig = Map[String, Vector[String]]

  private def read(path: String): Map[String, String] = {
    val content = """((?<!\")\+[a-zA-Z0-9]+)""".r
      .replaceAllIn(Source.fromFile(path).getLines.mkString("\n"), """\"$1\"""")

    ConfigFactory
      .parseString(content)
      .getConfig("retro")
      .entrySet()
      .asScala
      .map(e => e.getKey -> e.getValue.render())
      .toMap
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
          println()
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

  def apply(path: String): Either[java.lang.Throwable, Seq[DiagramConfiguration]] =
    try {
      Right(getDiagramsConfiguration(
        combineGeneralWithSpecifics(formatConfig(read(path)))))
    } catch {
      case ex => Left(ex)
    }
}
