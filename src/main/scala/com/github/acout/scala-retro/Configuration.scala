package com.github.acout.scalaretro.plugin

import java.io.{File, FileWriter}
import java.nio.file.Files

import com.github.acout.scalaretro.core.tokenizer.ScalaTokenizer
import com.github.acout.scalaretro.core.writer.MermaidClassDiagramWriter
import com.github.acout.scalaretro.core.filter.NameFilter
import com.github.acout.scalaretro.core.Utils
import com.github.acout.scalaretro.core.token.Token
import io.circe._
import io.circe.yaml.parser
import io.circe.generic.semiauto._
import sbt._

case class DiagramConfiguration(path: String,
                                outputFile: String,
                                tokenizer: String,
                                filters: Map[String, List[String]],
                                display: Map[String, Boolean]) {
  def runScalaRetro(log: Logger): Unit = {
    log.info("Creating Diagrams...")

    //Retrieve all ".scala" files recursively from one root folder
    val files = Utils.getAllScalaFiles(new File(path).toPath)
    //Tokenize the different files as a flatMapped list of Token
    val tokens = new ScalaTokenizer().tokenize(files)
    //Prepare the Mermaid.JS Class Diagram Writer with adequate output FileWriter
    val writer = new MermaidClassDiagramWriter(new FileWriter(new File(outputFile)))

    val unappliedFilters: List[Token => Boolean] = filters.toList.flatMap{
      case ("includes", regex) => Some(regex.map(r => NameFilter(r) _))
      case _ => None
    }.flatten
    val filteredTokens: List[Token] =
      unappliedFilters.foldLeft(tokens)((t: List[Token], f: Token => Boolean) => t.filter(f))

    //Serialize tokens as Mermaid.JS compatible markdown and write to the output file
    writer.write(filteredTokens)
    //Close the connection
    writer.close

    log.info(s"Successfully create Diagram in $outputFile from $path")
  }
}

object ConfigurationReader {
  implicit val decoder: Decoder[DiagramConfiguration] =
    deriveDecoder[DiagramConfiguration]

  def read(f: File, log: Logger): Option[List[DiagramConfiguration]] = {
    def yaml2json(yaml: File): Either[ParsingFailure, Json] =
      parser.parse(Files.readString(f.toPath))

    def decodeJson(json: Json): Either[DecodingFailure, List[DiagramConfiguration]] =
      json.as[List[DiagramConfiguration]]

    if (!f.exists()){
      log.warn(
        s"""Configuration file: $f not found. By default Scala retro will generate the diagram for all project sources (starting from the root).
           |`sbt retroReset` can be used to create a sample configuration file in the root directory.""".stripMargin)
      None
    }
    else {
      yaml2json(f) match {
        case Right(json) =>
          decodeJson(json) match {
            case Right(listConfig) =>
              log.info("Sucessfully read the config file...")
              Some(listConfig)
            case _                 =>
              log.error(s"""Error when reading $f. By default Scala retro will generate the diagram for all project sources (starting from the root).
                           |`sbt retroInit` can be used to create a sample configuration file in the root directory.""".stripMargin)
              None
          }
        case _ =>
          log.error(s"""Error when reading $f. By default Scala retro will generate the diagram for all project sources (starting from the root).
                       |`sbt retroInit` can be used to create a sample configuration file in the root directory.""".stripMargin)
          None
      }
    }
  }
}

