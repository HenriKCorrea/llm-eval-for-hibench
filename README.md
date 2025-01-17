# Requirements

- [Scala](https://www.scala-lang.org/download/)

# Challenges

Functional validity score:
- Package build
  - Scala syntax error: -1 point per error 

## Word count

### Changes made on original LLM code

#### WordCountLlamaApp.scala

```log
[error] /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/spark/src/main/scala/WordCountLlamaApp.scala:17:36: value value is not a member of org.apache.spark.sql.Row
[error]     val words = textFile.flatMap(_.value.split("\\s+"))
[error]            
```

```scala
    // Bugfix: adding missing import
    import spark.implicits._

    // Bugfix: Adding type cast .as[String]
    val textFile = spark.read.text(inputFilePath).as[String]

    // Bugfix: Remove get member .value
    val words = textFile.flatMap(_.split("\\s+"))
```

#### WordCountPhiApp.scala

```log
[error] /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/spark/src/main/scala/WordCountPhiApp.scala:20:25: Unable to find encoder for type String. An implicit Encoder[String] is needed to store String instances in a Dataset. Primitive types (Int, String, etc) and Product types (case classes) are supported by importing spark.implicits._  Support for serializing other types will be added in future releases.
[error]     val words = lines.as[String].flatMap(_.split("\\s+"))
[error]                         ^
```

```scala
    // Bugfix: adding missing import
    import spark.implicits._
```

### Lessons learned

- Missing prompt details:
  - Application will run in batch mode (to avoid streaming implementation).
  - Define application name to ensure consistency among LLM implementations and avoid manual refactor.

