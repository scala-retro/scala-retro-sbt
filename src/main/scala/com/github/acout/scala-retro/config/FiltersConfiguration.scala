package com.github.acout.scalaretro.plugin.config

case class FiltersConfiguration(includes: List[String],
                                excludes: List[String],
                                contains: List[String]) {

  def toList: List[(String, List[String])] =
    List("includes" -> includes, "excludes" -> excludes, "contains" -> contains)
}
