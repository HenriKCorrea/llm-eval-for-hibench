import org.apache.spark.sql.SparkSession

object WordCountLlamaApp {
  def main(args: Array[String]) {
    if (args.length != 2) {
      println("Usage: WordCountLlamaApp <input_file> <output_file>")
      System.exit(1)
    }

    val inputFilePath = args(0)
    val outputFilePath = args(1)

    val spark = SparkSession.builder.appName("Word Count Llama App").getOrCreate()

    import spark.implicits._

    val textFile = spark.read.text(inputFilePath).as[String]

    val words = textFile.flatMap(_.split("\\s+"))

    val wordCounts = words.groupBy("value").count()

    wordCounts.write.csv(outputFilePath)

    spark.stop()
  }
}