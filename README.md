# scala-retro-plugin

## How to use 

In `build.sbt`:

```scala
lazy val root = (project in file("."))
  .enablePlugins(com.github.acout.scalaretro.plugin.ScalaRetroPlugin)
  .settings(
    scalaVersion := XX,
    sbtVersion := XX,
    retroConfigFile := file("target/config.conf"),
  )
```

In `In project/plugins.sbt`:

```scala
resolvers += Resolver.bintrayRepo("acout", "maven")
libraryDependencies += "com.github.acout" %% "scala-retro-core" % "0.1.4"

addSbtPlugin("com.github.acout" % "sbt-retro" % "0.1")
```

## Commands 

- `sbt retroInit` :  Generate template configuration file
- `sb retro` : Generate diagrams (Compile the project before generation. `sbt doc` create the diagrams in addition to the documentation.)

## Scala retro configuration guide

### General rules

- The `retro` key is the root of the configuration.
- the "diagrams" key is mandatory to declare the diagrams to be configured.
- The general parameters are defined in the `retro` object.
- After defining your diagrams you can add them in the `retro` object.
- The values can be declared as an array or string (which will be considered as a one-element array when interpreting) 
- do not use special characters to avoid interpretation problems (except for merging parameters)

### Options

- `diagrams`: define diagrams by any name, so that the interpreter can distinguish between them
-  `src` : Sources of Scala files
- `tokenizer`: determines the tokenizer to use, by default "scala_2".
- `output` : path of generated markdown file
- `filters` : 
    - includes : includes the classes that match the regexs
    - excludes : excludes classes that match the regexs
    - contains : includes the classes that contains methods or fields that match the regex
    
### Add, subtract and overwrite parameters

- By default a parameter that is redefined in a diagram is overwritten
- the prefix `-` is used to make a subtraction of the parameters
- the prefix `+` is used to make an addition of the parameters


#### *To implement*

- `hide` : Element to hide on the diagram, can be fields, methods or dependencies 
- `show` : Element to show on the diagram, can be fields, methods or dependencies 
-  Add a prefix `^` for intersections?
- Add wildcard support

### Examples

#### Minimal example

```hocon
retro {
  diagrams : MyDiagram,
  MyDiagram {
    src : src/main/scala
    output : output.md
  }
}
```

#### Complete example

```hocon
retro {
  diagrams : [A, B]
  filters {
    includes : ["regex1", "regex2"]
    excludes : ["regex4", "regex3", "regex2"]
  }
  tokenizer : "scala_2"
  A {
    src : path/to/scala/file1
    output : xx1.md
    filters {
      -excludes : "regex4"
    }
  }
  B {
    src : [path/to/scala/file2, path/to/scala/file3]
    output : xx2.md
    filters {
      +includes : ["regex10"]
      contains : ["foo", "bar"]
    }
  }
}
```
