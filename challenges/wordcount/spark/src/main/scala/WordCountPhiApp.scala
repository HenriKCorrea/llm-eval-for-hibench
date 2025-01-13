import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object WordCountPhiApp {
  def main(args: Array[String]): Unit = {
    val inputFileUrl = args(0) // The URL of the input text file
    val outputFileUrl = args(1) // The URL where the output CSV file will be saved

    val spark = SparkSession.builder
      .appName("WordCountPhiApp")
      .getOrCreate()

    // Read the text file into a DataFrame
    val lines = spark.readStream
      .option("encoding", "UTF-8")
      .option("multiline", "true")
      .text(inputFileUrl)

    // Split the lines into words
    val words = lines.as[String].flatMap(_.split("\\s+"))

    // Generate running word count
    val wordCounts = words.groupBy("value").count()

    // Write the word counts to the output CSV file
    wordCounts.writeStream
      .outputMode("complete")
      .format("csv")
      .option("header", "true")
      .option("path", outputFileUrl)
      .option("checkpointLocation", "/path/to/checkpoint/directory")
      .start()

    spark.sparkContext.getConf.get("spark.sql.shuffle.partitions", "10")

    spark.streams.awaitAnyTermination()
  }
}