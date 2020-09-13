lazy val root = (project in file("."))
  .enablePlugins(com.github.acout.scalaretro.plugin.ScalaRetroPlugin)
  .settings(
    scalaVersion := "2.12.6",
    sbtVersion := "0.13.11",
    configFileDir := file("config.yaml"),
  )