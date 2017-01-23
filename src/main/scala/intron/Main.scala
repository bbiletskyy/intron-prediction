package intron

import org.apache.spark.sql._

object Main {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder()
      .appName("intron-prediction")
      .config("spark.master", "local[4]")
      .getOrCreate()
    import spark.implicits._
    val genes = Data.getValidGenes(spark.sparkContext).filter(_.exons.size == 2)
    println(s"Found ${genes.count()} valid genes.")
    genes.toDF().show(10)

    val gene = genes.first()
    println(gene)
    spark.stop()
  }
}
