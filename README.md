# Requirements

- [Scala](https://www.scala-lang.org/download/)

# Challenges

Functional validity score:
- Package build
  - Scala syntax error: -1 point per error 

## Word count

### Changes made on original LLM code

#### [WordCountLlamaApp.scala](challenges/wordcount/spark/src/main/scala/WordCountLlamaApp.scala)

Issues:
- Missing import
- Missing type cast
- Invalid member access

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

Issue: 
- Missing import to support [String] encoder.

```log
[error] /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/spark/src/main/scala/WordCountPhiApp.scala:20:25: Unable to find encoder for type String. An implicit Encoder[String] is needed to store String instances in a Dataset. Primitive types (Int, String, etc) and Product types (case classes) are supported by importing spark.implicits._  Support for serializing other types will be added in future releases.
[error]     val words = lines.as[String].flatMap(_.split("\\s+"))
[error]                         ^
```

```scala
    // Bugfix: adding missing import
    import spark.implicits._
```

### Challenge remarks

- WordCountPhiApp failed
```log
Exception in thread "main" org.apache.spark.sql.AnalysisException: Data source csv does not support Complete output mode.
```
```scala
    // Write the word counts to the output CSV file
    wordCounts.writeStream
      .outputMode("complete") // Exception root cause
      .format("csv")
      .option("header", "true")
      .option("path", outputFileUrl)
      .option("checkpointLocation", "/path/to/checkpoint/directory")
      .start()
```

### Static analysis

No errors found

```log
[info] compiling 3 Scala sources to /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/spark/target/scala-2.12/scapegoat-classes ...
[info] [info] [scapegoat] 121 activated inspections
[info] [info] [scapegoat] Analysis complete: 3 files - 0 errors 0 warns 0 infos
[info] [info] [scapegoat] Written HTML report [/home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/spark/target/scala-2.12/scapegoat-report/scapegoat.html]
```

See [List of inspections](https://github.com/scapegoat-scala/scapegoat?tab=readme-ov-file#inspections) for static analysis rules description.


### Lessons learned

- Missing prompt details:
  - Application will run in batch mode (to avoid streaming implementation).
  - Define application name to ensure consistency among LLM implementations and avoid manual refactor.

