package intron

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object Data {
  val DefaultDataPath = "./data/"
  val ExonsFileName = "exons.txt"
  val GenesFileName = "genes.txt"

  case class Exon(exonId: String, geneId: String, start: Long, end: Long, sequence: String) {
    lazy val length = end - start + 1
  }
  case class Gene(geneId: String, chromosomeName: String, start: Long, end: Long, sequence: String, exons: Seq[Exon] = Nil, exonIds: Seq[String] = Nil) {
    lazy val length = end - start + 1
    lazy val exonsLength = exons.map(_.length).sum
  }

  def getDataPath(args: Array[String]): String = {
    if (!args.isEmpty) args(0) else {
      println(s"Data folder was not specified, setting to default: ${DefaultDataPath}.")
      println("""To specify data folder run as: sbt "run path/to/data" """)
      DefaultDataPath
    }
  }

  def isValid(gene: Gene): Boolean  =  {
    (gene.exons.map(_.exonId).toSet == gene.exonIds.toSet) || gene.exons.forall(exon => gene.sequence.contains(exon.sequence))
  }

  def getValidGenes(sc: SparkContext, dataPath: String, validator: Gene => Boolean = isValid) = getGenes(sc, dataPath).filter(validator(_))

  def getGenes(sc: SparkContext, dataPath: String) = {
    val exons = getExons(sc, s"$dataPath/$ExonsFileName")
    val genes = getGenesWithoutExons(sc, s"$dataPath/$GenesFileName")
    val geneIdExons: RDD[(String, Iterable[Exon])] = exons.groupBy(_.geneId)
    genes
      .map(g => (g.geneId, g))
      .leftOuterJoin(geneIdExons)
      .map { case (geneId, (gene, geneExons)) => (gene, geneExons.getOrElse(Nil).toSeq.sortBy(_.start)) }
      .map { case (gene, geneExons) => gene.copy(exons = geneExons) }
  }

  def getGenesWithoutExons(sc: SparkContext, genesFile: String): RDD[Gene] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter", ">")
    val rows = sc.textFile(genesFile).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)
    def geneId(cs: Array[String]) = cs(0)
    def chrName(cs: Array[String]) = cs(1)
    def geneStart(cs: Array[String]) = cs(2).toLong
    def geneEnd(cs: Array[String]) = cs(3).toLong
    def exonIds(cs: Array[String]) = cs(11).split(';').filter(!_.trim.isEmpty)
    def geneSequence(cs: Array[String]) = cs(14)

    rows.map(row => {
      val cols = row.split('|')
      Gene(geneId = geneId(cols), chromosomeName = chrName(cols), start = geneStart(cols), end = geneEnd(cols), sequence = geneSequence(cols), exonIds = exonIds(cols))
    })
  }

  def getExons(sc: SparkContext, exonsFile: String): RDD[Exon] = {
    sc.hadoopConfiguration.set("textinputformat.record.delimiter", ">")
    val rows = sc.textFile(exonsFile).map(_.trim.replaceFirst("\n", "|").replace("\n", "")).filter(!_.isEmpty)
    def geneId(cs: Array[String]) = cs(0)
    def exonId(cs: Array[String]) = cs(11)
    def start(cs: Array[String]) = cs(12).toLong
    def end(cs: Array[String]) = cs(13).toLong
    def sequence(cs: Array[String]) = cs(14)
    rows.map(row => {
      val cols = row.split('|')
      Exon(geneId = geneId(cols), exonId = exonId(cols), start = start(cols), end = end(cols), sequence = sequence(cols))
    })
  }
}
