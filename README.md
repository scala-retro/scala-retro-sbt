# Scala Retro SBT plugin

A sbt plugin for [Scala Retro](https://github.com/acout/scala-retro), a package that allows to generate UML Class Diagrams from Scala source code.

## Getting Started

### Installing

To begin using the package, setup your `build.sbt` by adding the following:

```scala
enablePlugins(com.github.acout.scalaretro.plugin.ScalaRetroPlugin)
retroConfigFile := file("target/config.conf")
```

And in `In project/plugins.sbt`:

```scala
resolvers += Resolver.bintrayRepo("acout", "maven")
libraryDependencies += "com.github.acout" %% "scala-retro-core" % "0.1.4"

addSbtPlugin("com.github.acout" % "sbt-retro" % "0.1")
```

### SBT tasks

- `sbt retroInit` :  Generate the template configuration file.
- `sb retro` : Generate diagrams (Compile the project before generation. `sbt doc` create the diagrams in addition to the documentation).

## Configuration

### General rules

- The location of your location file depends on the `retroConfigFile` key in `build.sbt`.
- The configuration files use the HOCON standard and can be substituted with JSON.
- The `retro` key is the root of the configuration.
- the "diagrams" key is mandatory to declare the diagrams to be configured.
- The general parameters are defined in the `retro` object.
- The values can be declared as an array or string (which will be considered as a one-element array when interpreting).
- Don't use special characters to avoid interpretation problems (except for merging parameters).

### Options

- `diagrams`: Array that contains parameters of diagrams.
-  `src` : Sources of Scala files.
- `tokenizer`: determines the tokenizer to use, by default "scala_2".
- `output` : path of generated markdown file.
- `filters` : 
    - `includes` : includes the classes that match the regex.
    - `excludes` : excludes classes that match the regex.
    - `contains` : includes the classes that contains methods or fields that match the regex.
    
### Add, subtract and overwrite parameters

- By default a parameter that is redefined in a diagram is overwritten.
- the prefix `-` is used to make a subtraction of the parameters.
- the prefix `+` is used to make an addition of the parameters.

### Examples

#### Minimal example

```hocon
retro {
  diagrams : [
    {
      src : src/main/scala
      output : output.md
    }
  ]
}
```

#### Complete example

```hocon
retro {
  filters {
    includes : ["regex1", "regex2"]
    excludes : ["regex4", "regex3", "regex2"]
  }
  tokenizer : "scala_2"
  diagrams : [{
    src : path/to/scala/file1
    output : xx1.md
    filters {
      -excludes : "regex4"
    }
  }, {
    src : [path/to/scala/file2, path/to/scala/file3]
    output : xx2.md
    filters {
      +includes : ["regex10"]
      contains : ["foo", "bar"]
    }
  }]
}
```

## Build With

- [Scala Retro](https://github.com/acout/scala-retro) - The core library
- [Config](https://github.com/lightbend/config) - Used to read configuration files