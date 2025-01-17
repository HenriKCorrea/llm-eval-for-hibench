# Requirements

- [Scala](https://www.scala-lang.org/download/)
- [Docker](https://docs.docker.com/engine/install/ubuntu/)
- [Docker Compose](https://docs.docker.com/compose/install/)

# Challenges

## Word count

### [WordCountGeminiApp.scala](challenges/wordcount/spark/src/main/scala/WordCountGeminiApp.scala)

- Compiled successfully. No intervention required.
- Code executed with success!

### [WordCountLlamaApp.scala](challenges/wordcount/spark/src/main/scala/WordCountLlamaApp.scala)

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

### [WordCountPhiApp.scala](challenges/wordcount/spark/src/main/scala/WordCountPhiApp.scala)

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

- WordCountPhiApp application failed
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

### [WordCountGeminiFlinkApp.scala](challenges/wordcount/flink/src/main/scala/WordCountGeminiFlinkApp.scala)

- Compiled successfully. No intervention required.

### [WordCountLlamaFlinkApp.scala](challenges/wordcount/flink/src/main/scala/WordCountLlamaFlinkApp.scala)

Issue: 
- Missing import to provide scala API return types.

```log
[error] /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/flink/src/main/scala/WordCountLlamaFlinkApp.scala:18:15: could not find implicit value for evidence parameter of type org.apache.flink.api.common.typeinfo.TypeInformation[(String, Int)]
[error]       .flatMap(new Tokenizer)
```
```scala
    // Bugfix: adding missing import
    import org.apache.flink.api.scala._
```


### [WordCountPhiFlinkApp](challenges/wordcount/flink/src/main/scala/WordCountPhiFlinkApp.scala)

- Build failed
  - Hallucination: generated many import and object members that does not exists

```log
[error]                                             ^
[error] /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/flink/src/main/scala/WordCountPhiFlinkApp.scala:17:45: object functions is not a member of package org.apache.flink.streaming.api.scala
[error] import org.apache.flink.streaming.api.scala.functions.RichWindowFunction
[error]                                             ^
[error] /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/flink/src/main/scala/WordCountPhiFlinkApp.scala:29:13: could not find implicit value for evidence parameter of type org.apache.flink.api.common.typeinfo.TypeInformation[String]
[error]     .flatMap(_.toLowerCase.split("\\W+"))
[error]             ^
[error] 9 errors found
[error] (Compile / compileIncremental) Compilation failed
[error] Total time: 3 s, completed Jan 17, 2025 4:55:06 AM
```

### Static analysis

[Scapegoat](https://github.com/scapegoat-scala/scapegoat) has been used for Static analysis. No errors found.

#### Spark source code static analysis

```log
[info] compiling 3 Scala sources to /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/spark/target/scala-2.12/scapegoat-classes ...
[info] [info] [scapegoat] 121 activated inspections
[info] [info] [scapegoat] Analysis complete: 3 files - 0 errors 0 warns 0 infos
[info] [info] [scapegoat] Written HTML report [/home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/spark/target/scala-2.12/scapegoat-report/scapegoat.html]
```

#### Flink source code static analysis

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

docker stop flink; docker rm flink; docker run --detach --name flink -p 8081:8081 flink:1.13.0-scala_2.12-java11 /docker-entrypoint.sh jobmanager && docker exec --detach flink /docker-entrypoint.sh taskmanager && docker exec --user flink flink mkdir -p /opt/flink/work-dir/input/ && docker cp /home/henrique/repo/llm-eval-for-hibench/challenges/input/dracula.txt flink:/opt/flink/work-dir/input/ && docker exec flink flink run examples/batch/WordCount.jar --input /opt/flink/work-dir/input/dracula.txt --output /opt/flink/work-dir/AppOutput.txt && docker cp flink:/opt/flink/work-dir/AppOutput.txt /home/henrique/repo/llm-eval-for-hibench/challenges/output/ && docker stop flink && docker rm flink
