import org.apache.flink.api.scala._

object WordCountGeminiFlinkApp {
  def main(args: Array[String]) {

    val inputPath = "/path/to/input/file" // Replace with your input file path
    val outputPath = "/path/to/output/file" // Replace with your desired output path

    val env = ExecutionEnvironment.getExecutionEnvironment

    // get input data
    val text = env.readTextFile(inputPath)

    val counts = text.flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
      .map { (_, 1) }
      .groupBy(0)
      .sum(1)

    counts.writeAsCsv(outputPath, "\n", " ")

    env.execute("WordCountGeminiFlinkApp")
  }
}