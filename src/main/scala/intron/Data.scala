package intron

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object Data {
  val DataPath = "./data"
  val ExonsFilePath = DataPath + "/exons.txt"
  val GenesFilePath = DataPath + "/genes.txt"
  case class Exon(exonId: String, geneId: String, sequence: String)
  case class Gene(geneId: String, chromosomeName: String, sequence: String)

  def main(args: Array[String]) {
    val sc = new SparkContext("local[4]", "intron-prediction")
    val genes = getGenes(sc, GenesFilePath)
    val exons = getExons(sc, ExonsFilePath)
  }

  def getGenes(sc: SparkContext, genesFile: String): RDD[Gene] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter", ">")
    val rows = sc.textFile(genesFile).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)
    rows.map(row => {
      val cols = row.split('|')
      Gene(geneId = cols(0), chromosomeName = cols(1), sequence = cols(14))
    })
  }

  def getExons(sc: SparkContext, exonsFile: String): RDD[Exon] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter", ">")
    val rows = sc.textFile(exonsFile).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)

    rows.map(row => {
      val cols = row.split('|')
      Exon(geneId = cols(0), exonId = cols(11), sequence = cols(14)) //fields(14)
    })
  }

}
