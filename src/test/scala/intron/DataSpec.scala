package intron

import org.apache.spark.SparkContext
import org.scalatest.{ BeforeAndAfterAll, FlatSpec }

class DataSpec extends FlatSpec with BeforeAndAfterAll {
  var sc: SparkContext = _
  val ExonsFilePath: String = getClass.getResource("/data/exons.sample").toString
  val GenesFilePath: String = getClass.getResource("/data/genes.sample").toString

  override def beforeAll() = {
    sc = new SparkContext("local[4]", "intron-prediction-test")
  }

  override def afterAll() = {
    if (sc != null) {
      sc.stop()
    }
  }

  "Data" should "get genes correctly" in {
    assert(Data.getGenes(sc, GenesFilePath).count() == 11)
  }

  "Data" should "get exons correctly" in {
    assert(Data.getExons(sc, ExonsFilePath).count() == 16)
  }

  "Data" should "be aligned between genes and exons" in {
    val exons = Data.getExons(sc, ExonsFilePath)

    assert(Data.getGenes(sc, GenesFilePath).takeSample(true, 10).forall { gene =>
      val geneExons = exons.filter(_.geneId == gene.geneId).collect()
      geneExons.forall(geneExon => gene.sequence.contains(geneExon.sequence))
    })
  }

}
