package intron

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object Data {
  val DefaultDataPath = "./data/"
  val ExonsFileName = "exons.txt"
  val GenesFileName = "genes.txt"
  val DefaultExonsPath = DefaultDataPath + ExonsFileName
  val DefaultGenesPath = DefaultDataPath + GenesFileName

  def defaultValidator(gene: Gene): Boolean = {
    (gene.exons.map(_.exonId).toSet == gene.exonIds.toSet) || gene.exons.forall(exon => gene.sequence.contains(exon.sequence))
  }

  /**
   * Gets genes enriched with exons and filters out the genes that did not pass validation
   * @param sc - [SparkContext]
   * @param dataPath - path to data files, optional
   * @param validator - custom validator for filtering out inconsistent data
   * @return [RDD] of [Gene]
   */
  def getValidGenes(sc: SparkContext, dataPath: String = DefaultDataPath, validator: Gene => Boolean = defaultValidator) = getGenes(sc, dataPath).filter(validator(_))

  def getGenes(sc: SparkContext, dataPath: String = DefaultDataPath) = {
    val exons = getExons(sc, s"$dataPath/$ExonsFileName")
    val genes = getGenesWithoutExons(sc, s"$dataPath/$GenesFileName")
    val geneIdExons: RDD[(String, Iterable[Exon])] = exons.groupBy(_.geneId)
    genes
      .map(g => (g.geneId, g))
      .leftOuterJoin(geneIdExons)
      .map { case (geneId, (gene, geneExons)) => (gene, geneExons.getOrElse(Nil).toList.sortBy(exon => gene.sequence.indexOf(exon.sequence))) }
      .map { case (gene, geneExons) => gene.copy(exons = geneExons) }
  }

  def getGenesWithoutExons(sc: SparkContext, genesFile: String = DefaultGenesPath): RDD[Gene] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter", ">")
    val rows = sc.textFile(genesFile).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)
    def geneId(cs: Array[String]) = cs(0)
    def exonIds(cs: Array[String]) = cs(1).split(';').filter(!_.trim.isEmpty).toList
    def geneSequence(cs: Array[String]) = cs(2)

    rows.map(row => {
      val cols = row.split('|')
      Gene(geneId = geneId(cols), exonIds = exonIds(cols), sequence = geneSequence(cols))
    })
  }

  def getExons(sc: SparkContext, exonsFile: String = DefaultExonsPath): RDD[Exon] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter", ">")
    val rows = sc.textFile(exonsFile).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)
    def geneId(cs: Array[String]) = cs(0)
    def exonId(cs: Array[String]) = cs(1)
    def sequence(cs: Array[String]) = cs(2)
    rows.map(row => {
      val cols = row.split('|')
      Exon(geneId = geneId(cols), exonId = exonId(cols), sequence = sequence(cols))
    })
  }
}
