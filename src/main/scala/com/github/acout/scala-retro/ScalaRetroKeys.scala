package com.github.acout.scalaretro.plugin

import sbt._

trait ScalaRetroKeys {
  lazy val configFileDir = settingKey[File]("source directory of config file")

  lazy val retro = taskKey[Unit]("Generate UML Class Diagrams from source code")

  lazy val retroInit = taskKey[Unit]("Create an model YAML config file in root directory")
}
