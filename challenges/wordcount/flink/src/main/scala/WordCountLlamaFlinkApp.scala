import org.apache.flink.api.common.functions.{FlatMapFunction, ReduceFunction}
import org.apache.flink.api.common.typeinfo.Types
import org.apache.flink.api.java.tuple.Tuple2
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.util.Collector
import org.apache.flink.api.scala._

object WordCountLlamaFlinkApp {

  def main(args: Array[String]): Unit = {
    // Set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // Read the input file
    val text: DataStream[String] = env.readTextFile("path_to_your_input_file.txt")

    // Process the data
    val counts: DataStream[(String, Int)] = text
      .flatMap(new Tokenizer)
      .keyBy(0)
      .reduce(new SumReducer)

    // Write the data to the output CSV file
    counts.writeAsCsv("path_to_your_output_file.csv")

    // Execute the program
    env.execute("WordCountLlamaFlinkApp")
  }
}

class Tokenizer extends FlatMapFunction[String, (String, Int)] {
  override def flatMap(value: String, out: Collector[(String, Int)]): Unit = {
    // Split the line by spaces and emit each word with a count of 1
    value.toLowerCase.split("\\s+").foreach { word =>
      out.collect((word, 1))
    }
  }
}

class SumReducer extends ReduceFunction[(String, Int)] {
  override def reduce(value1: (String, Int), value2: (String, Int)): (String, Int) = {
    // Reduce two values of type (String, Int) into one
    (value1._1, value1._2 + value2._2)
  }
}