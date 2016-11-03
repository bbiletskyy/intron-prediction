import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

object Main {
  def main(args: Array[String]) {
    val logFile = "README.md"
    val sc = new SparkContext("local[4]", "intron-prediction")
    val logData = sc.textFile(logFile, 2).cache()
    val numAs = logData.filter(_.contains("a")).count()
    val numBs = logData.filter(_.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")
  }
}
