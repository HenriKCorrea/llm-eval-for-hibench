
val sparkSql = "org.apache.spark" %% "spark-sql" % "3.5.4"

name := "wordcount-gemini-app"
version := "1.0"
scalaVersion := "2.12.18"
libraryDependencies += sparkSql