package com.github.acout.scalaretro.plugin.config

import java.io.{File, FileWriter}

import com.github.acout.scalaretro.core.Utils
import com.github.acout.scalaretro.core.filter.NameFilter
import com.github.acout.scalaretro.core.token.Token
import com.github.acout.scalaretro.core.tokenizer.ScalaTokenizer
import com.github.acout.scalaretro.core.writer.MermaidClassDiagramWriter
import sbt._

case class DiagramConfiguration(name: String,
                                srcs: List[String],
                                output: Option[String],
                                filters: FiltersConfiguration,
                                tokenizer: Option[String]) {
  def generate(log: Logger): Unit = {
    log.info("Creating Diagrams...")

    lazy val unappliedFilters: List[Token => Boolean] = filters.toList.flatMap {
      case ("includes", regex) => Some(regex.map(r => NameFilter(r) _))
      case _                   => None
    }.flatten

    srcs.zipWithIndex.foreach {
      case (path, i) =>
        //Retrieve all ".scala" files recursively from one root folder
        val files = Utils.getAllScalaFiles(new File(path).toPath)
        //Tokenize the different files as a flatMapped list of Token
        val tokens = new ScalaTokenizer().tokenize(files)
        //Prepare the Mermaid.JS Class Diagram Writer with adequate output FileWriter
        val Array(base, extension) =
          output.getOrElse(s"$name.md").split("\\.(?=[^\\.]+$)")
        val outputFileName =
          if (srcs.length == 1) base + "." + extension
          else base + s"_$i." + extension
        val writer =
          new MermaidClassDiagramWriter(
            new FileWriter(new File(outputFileName)))

        val filteredTokens: List[Token] =
          unappliedFilters.foldLeft(tokens)(
            (t: List[Token], f: Token => Boolean) => t.filter(f))

        //Serialize tokens as Mermaid.JS compatible markdown and write to the output file
        writer.write(filteredTokens)
        //Close the connection
        writer.close

        log.info(s"Successfully create Diagram in $outputFileName from $path")
    }
  }
}
