import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

object Main {

  val DataPath = "./data"
  val ExonsFilePath = DataPath + "/exons.txt"
  val GenesFilePath = DataPath + "/genes.txt"
  case class Exon(exonId: String, geneId: String, sequence: String)
  case class Gene(geneId: String, chromosomeName: String, sequence: String)

  def main(args: Array[String]) {

    val sc = new SparkContext("local[4]", "intron-prediction")

    //print all genles and exons assosiated wit hthem
    exons(sc).groupBy(_.geneId).map { case (geneId, exons) => (geneId, exons.size) }.foreach(println)
  }


  def genes(sc: SparkContext): RDD[Gene] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter",">")
    val rows = sc.textFile(GenesFilePath).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)
    val genes = rows.map(row => {
      val cols = row.split('|')
      Gene(geneId = cols(0), chromosomeName = cols(1), sequence = "")
    })
    genes
  }

  def exons(sc: SparkContext):RDD[Exon] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter",">")
    val rows = sc.textFile(ExonsFilePath).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)
    val exons = rows.map(row => {
      val fields = row.split('|')
      Exon(geneId = fields(0), exonId = fields(11), sequence = "")//fields(14)
    })
    return exons
  }

}
