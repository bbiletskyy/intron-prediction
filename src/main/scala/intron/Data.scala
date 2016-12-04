package intron

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object Data {
  val ExonsFileName = "exons.txt"
  val GenesFileName = "genes.txt"

  case class Exon(exonId: String, geneId: String, sequence: String)
  case class Gene(geneId: String, chromosomeName: String, sequence: String)

  def main(args: Array[String]) {
    if (args.length == 0) {
      println("""Data folder was not specified. Run as: sbt "run path/to/data/files" """)
      return
    }

    val genesFilePath = args(0) + "/" + GenesFileName
    val exonsFilePath = args(0) + "/" + ExonsFileName

    val sc = new SparkContext("local[4]", "intron-prediction")
    println("Checking data...")
    println(s"Raeding genes from $genesFilePath...")
    val genes = getGenes(sc, genesFilePath)
    println(s"Total genes read:  ${genes.count()} ")
    println(s"Raeding exons from $exonsFilePath...")
    val exons = getExons(sc, exonsFilePath)
    println(s"Total exons read:  ${exons.count()} ")

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
      Exon(geneId = cols(0), exonId = cols(11), sequence = cols(14))
    })
  }

}
