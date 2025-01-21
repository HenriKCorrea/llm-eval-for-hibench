# Activities overview
  1. Article study
     1. [Ságodi, Siket, and Ferenc, “Methodology for Code Synthesis Evaluation of LLMs Presented by a Case Study of ChatGPT and Copilot.”](https://ieeexplore.ieee.org/document/10535504)
     2. [Helmuth and Kelly, “PSB2: the second program synthesis benchmark suite”](https://arxiv.org/abs/2106.06086)
     3. [White et al., “ChatGPT Prompt Patterns for Improving Code Quality, Refactoring, Requirements Elicitation, and Software Design.”](https://arxiv.org/abs/2303.07839)
     4. [Gao et al., “Retrieval-Augmented Generation for Large Language Models.”](https://arxiv.org/abs/2312.10997)
  2. Research free LLM API providers
     1. [Google AI Studio](https://aistudio.google.com)
     2. [OpenRounter](https://openrouter.ai)
     3. [Hugging Face](https://huggingface.co)
     4. [Claude](https://claude.ai)
  3. Research [Docker](https://www.docker.com/) services to setup local development environment
     1. [LiteLLM](https://www.litellm.ai) -> [Docker image](https://github.com/berriai/litellm/pkgs/container/litellm/332151012?tag=main-v1.57.1)
     2. [Open WebUI](https://openwebui.com/) -> [Docker image](https://github.com/open-webui/open-webui/pkgs/container/open-webui/331304257?tag=git-1dfb479)
     3. [Apache Spark](https://spark.apache.org/) -> [Docker image](https://hub.docker.com/layers/apache/spark/3.5.4-scala2.12-java11-python3-r-ubuntu/images/sha256-ac42623989f47e5c9f12be0ff4bf3f26929d2145aecc68c38e30de16586040a5)
     4. [Apache Flink](https://flink.apache.org/) -> [Docker image](https://hub.docker.com/layers/library/flink/1.13.0-scala_2.12-java11/images/sha256-d98f3ebb38bc662a35e7d0352df475d2e90ea822b717450d4187d23ccb73a06b)
  4. Deploy local development environment
  5. Test LLM API communication
  6. Choose LLM candidates for this work
     1. [Gemini 2.0 Flash Experimental](https://blog.google/technology/google-deepmind/google-gemini-ai-update-december-2024/#ceo-message)
     2. [Llama 3.1 70b instruct](https://ai.meta.com/research/publications/the-llama-3-herd-of-models/)
     3. [Phi 3 mini instruct](https://arxiv.org/abs/2404.14219)
  7. Create knowledge base from framework documentation
     1. [Apache Spark version 3.1.1 documentation](/knowledge/docs_spark_3.1.1/)
     2. [Apache Flink 1.13 documentation](/knowledge/docs_flink_1.13/)
  8. Create challenge prompt
     1. `Generate a <framework_name> application using Scala language to solve the problem! Given a plain text UTF-8 file URL and a output CSV file URL, write in the output file the occurrence sum of each word in the input file.`
  9. Execute prompt for each LLM
  10. Develop Scala project
  11. Build script to run challenge on framework docker image
  12. Build code generated from LLM output
  13. Run static analisys
      1.  [Scapegoat](https://github.com/scapegoat-scala/scapegoat)
  14. Search input text for application
      1.  [Dracula by Bram Stoker](https://www.gutenberg.org/ebooks/345)
  15. Run code
  16. Evaluate output
  17. Write presentation

# Deliverables Artifacts

# Local development environment requirements

- [Scala](https://www.scala-lang.org/download/)
- [Docker](https://docs.docker.com/engine/install/ubuntu/)
- [Docker Compose](https://docs.docker.com/compose/install/)

# Challenges

## Word count

### [WordCountGeminiApp.scala](challenges/wordcount/spark/src/main/scala/WordCountGeminiApp.scala)

- Compiled successfully. No intervention required.
- Code executed with success!

### [WordCountLlamaApp.scala](challenges/wordcount/spark/src/main/scala/WordCountLlamaApp.scala)

Build Issues:
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

Code executed with success!

### [WordCountPhiApp.scala](challenges/wordcount/spark/src/main/scala/WordCountPhiApp.scala)

Issue: 
- Missing import to support [String] encoder.
- Application failed

```log
[error] /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/spark/src/main/scala/WordCountPhiApp.scala:20:25: Unable to find encoder for type String. An implicit Encoder[String] is needed to store String instances in a Dataset. Primitive types (Int, String, etc) and Product types (case classes) are supported by importing spark.implicits._  Support for serializing other types will be added in future releases.
[error]     val words = lines.as[String].flatMap(_.split("\\s+"))
[error]                         ^
```

```scala
    // Bugfix: adding missing import
    import spark.implicits._
```

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
- Application failed

```text
------------------------------------------------------------
 The program finished with the following exception:

org.apache.flink.client.program.ProgramInvocationException: Neither a 'Main-Class', nor a 'program-class' entry was found in the jar file.
```

### [WordCountLlamaFlinkApp.scala](challenges/wordcount/flink/src/main/scala/WordCountLlamaFlinkApp.scala)

Issue: 
- Missing import to provide scala API return types.
- Application failed

```log
[error] /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/flink/src/main/scala/WordCountLlamaFlinkApp.scala:18:15: could not find implicit value for evidence parameter of type org.apache.flink.api.common.typeinfo.TypeInformation[(String, Int)]
[error]       .flatMap(new Tokenizer)
```
```scala
    // Bugfix: adding missing import
    import org.apache.flink.api.scala._
```

```text
------------------------------------------------------------
 The program finished with the following exception:

org.apache.flink.client.program.ProgramInvocationException: Neither a 'Main-Class', nor a 'program-class' entry was found in the jar file.
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
