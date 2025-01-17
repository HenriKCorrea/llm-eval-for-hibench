import org.apache.spark.sql.SparkSession

object WordCountGeminiApp {
  def main(args: Array[String]): Unit = {

    if (args.length != 2) {
      System.err.println("Usage: WordCountGeminiApp <input-file> <output-file>")
      System.exit(1)
    }

    val inputFile = args(0)
    val outputFile = args(1)

    val spark = SparkSession
      .builder()
      .appName("Word Count Gemini App")
      .getOrCreate()

    import spark.implicits._

    // Read the text file into a Dataset of Strings
    val textFile = spark.read.text(inputFile).as[String]

    // Split each line into words and flatten the Dataset
    val words = textFile.flatMap(_.split(" "))

    // Group by word and count occurrences
    val wordCounts = words.groupBy("value").count()

    // Write the result to a CSV file
    wordCounts.coalesce(1).write
      .option("header", "true")
      .csv(outputFile)

    spark.stop()
  }
}