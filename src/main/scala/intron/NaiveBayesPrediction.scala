package intron

import intron.Data.Gene
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.regression.LabeledPoint


/** Predicts intron/exon sequences in gene sequence */
object NaiveBayesPrediction {

  def main(args: Array[String]) {
    val sc = new SparkContext("local[4]", "intron-prediction")
    val data = Data.getValidGenes(sc, Data.getDataPath(args)).cache()
    def toLabledPoints(g: Gene): Seq[LabeledPoint] = {
      def exonSymbols(n: Int) = List.fill(n)('1').mkString
      val ys = g.exons.map(_.sequence)
        .fold(g.sequence)((geneSeq, exonSeq) => geneSeq.replace(exonSeq, exonSymbols(exonSeq.length)))
        .replaceAll("[ACGT]", "0").map(_.asDigit)
      val xs = g.sequence.map(_.toUpper).map {
        case 'A' => 0
        case 'C' => 1
        case 'G' => 2
        case 'T' => 3
        case 'N' => 4
        case 'F' => 5
      }
      ys.zip(xs).map {case (y, x) => LabeledPoint(y, Vectors.dense(x))}
    }
    val Array(train, test) = data.sample(true, 0.001).flatMap(toLabledPoints(_)).randomSplit(Array(0.6, 0.4))
    val model = NaiveBayes.train(train)
    val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
    val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()
    println(s"Accuracy: ${accuracy}")
  }


}

