package intron

import org.apache.spark.sql.{DataFrame, SparkSession}

object Main {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder()
      .appName("intron-prediction")
      .config("spark.master", "local[4]")
      .getOrCreate()
    import spark.implicits._
    val genes = Data.getValidGenes(spark.sparkContext).toDF()
    genes.show(10)
    println(s"Found ${genes.count()} valid genes.")
    spark.stop()
  }
}
