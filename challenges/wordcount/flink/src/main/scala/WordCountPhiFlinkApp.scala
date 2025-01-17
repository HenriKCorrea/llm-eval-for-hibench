import org.apache.flink.api.java.ExecutionEnvironment
import org.apache.flink.api.scala.DataSet
import org.apache.flink.api.java.tuple.Tuple2
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.input.FileInputFormat
import org.apache.flink.streaming.api.scala.output.TextOutput
import org.apache.flink.streaming.api.scala.{StreamFunction, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.functions.sink.FileSink
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.api.windowing.assigners.CountWindowAssigner
import org.apache.flink.streaming.api.windowing.windows.GlobalWindows
import org.apache.flink.streaming.api.functions.windowing.WindowFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.functions.RichWindowFunction
import org.apache.flink.streaming.api.scala.functions.RichWindowFunction

object WordCountPhiFlinkApp extends App {

  val env = StreamExecutionEnvironment.getExecutionEnvironment

  // Read the input file
  val inputFile = "input-file-url"
  val text = env.readTextFile(inputFile)

  // Split the text into words and count occurrences
  val words = text
    .flatMap(_.toLowerCase.split("\\W+"))
    .filter(_.nonEmpty)
    .map((_, 1))

  // Group by word and count occurrences
  val wordCounts = words
    .keyBy(0)
    .sum(1)

  // Write the results to the output CSV file
  val outputFile = "output-csv-file-url"
  wordCounts.writeAsText(outputFile)

  env.execute("WordCountPhiFlinkApp")
}