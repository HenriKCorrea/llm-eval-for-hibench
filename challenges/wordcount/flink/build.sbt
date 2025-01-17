name := "flink-wordcount-app"
version := "1.0"
scalaVersion := "2.12.18"
libraryDependencies ++= Seq(
  "org.apache.flink" %% "flink-scala" % "1.14.4",
  "org.apache.flink" %% "flink-streaming-scala" % "1.14.4"
)

unmanagedSources in Compile := (unmanagedSources in Compile).value.filterNot(_.getName == "WordCountPhiFlinkApp.scala")
