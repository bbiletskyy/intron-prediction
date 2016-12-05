package intron

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object Data {
  val DefaultDataPath = "./data/"
  val ExonsFileName = "exons.txt"
  val GenesFileName = "genes.txt"

  case class Exon(exonId: String, geneId: String, sequence: String)
  case class Gene(geneId: String, chromosomeName: String, sequence: String, exons: Seq[Exon]=Nil, exonIds: Seq[String] = Nil)

  def main(args: Array[String]) {
    val dataPath = if (args.length == 0) {
      println(
        s"""Data folder was not specified, setting to default: ${DefaultDataPath}.
          |To specify data folder run as: sbt "run path/to/data" """.stripMargin)
      DefaultDataPath
    } else {
      args(0)
    }
    val genesFilePath = dataPath + "/" + GenesFileName
    val exonsFilePath = dataPath + "/" + ExonsFileName
    val sc = new SparkContext("local[4]", "intron-prediction")
    val dirtyGenes = getGenes(sc, genesFilePath, exonsFilePath)
    val corruptGenes = dirtyGenes.filter(g => g.exons.map(_.exonId).toSet != g.exonIds.toSet).collect()
    println(s"${corruptGenes.length} corrupt genes detected:")
    corruptGenes.foreach(println)
    val genes = dirtyGenes.filter(g => g.exons.map(_.exonId).toSet == g.exonIds.toSet)
    println(s"Genes count: ${genes.count()}")
  }


  def getGenes(sc: SparkContext, genesFile: String, exonsFile: String) = {
    val exons = getExons(sc, exonsFile)
    val genes = getGenesWithoutExons(sc, genesFile)
    val geneIdExons: RDD[(String, Iterable[Exon])] = exons.groupBy(_.geneId)
    genes
      .map(g => (g.geneId, g))
      .leftOuterJoin(geneIdExons)
      .map{case (geneId, (gene, geneExons)) => (gene, geneExons.getOrElse(Nil).toSeq)}
      .map{case (gene, geneExons) => gene.copy(exons = geneExons)}
  }

  def getGenesWithoutExons(sc: SparkContext, genesFile: String): RDD[Gene] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter", ">")
    val rows = sc.textFile(genesFile).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)
    rows.map(row => {
      val cols = row.split('|')
      val geneExonIds = cols(11).split(';').filter(!_.trim.isEmpty)
      val seqence = cols(14)
      Gene(geneId = cols(0), chromosomeName = cols(1), sequence = "", exonIds = geneExonIds)
    })
  }

  def getExons(sc: SparkContext, exonsFile: String): RDD[Exon] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter", ">")
    val rows = sc.textFile(exonsFile).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)
    rows.map(row => {
      val cols = row.split('|')
      val seqence = cols(14)
      Exon(geneId = cols(0), exonId = cols(11), sequence = "")
    })
  }

}
