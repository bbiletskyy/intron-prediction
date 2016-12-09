package intron

import intron.Data.{Exon, Gene}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD

/** Predicts intron/exon sequences in gene sequence */
object NaiveBayesPrediction {

  case class Example(input: String, output: String)

  val NucleotideId = Map('A' -> 0, 'C' -> 1, 'G' -> 2, 'T' -> 3)

  def main(args: Array[String]) {
    val sc = new SparkContext("local[4]", "intron-prediction")
    val data = Data.getGenes(sc, Data.getDataPath(args)).cache()

    def toLabledPoints(g: Gene) = {
      g.exons
        .map(_.sequence)
        .fold(g.sequence)((a, b) => a.replace(b, List.fill(b.length)('1').mkString))
        .replaceAll("[ACGT]", "0")
        .map(_.asDigit)
    }

    val sample = data.take(10).map(g => toLabledPoints(g)).foreach(println)



    //    def markExons(g:Gene, exonLabel: String = "1", intronLabel: String = "0"): String = g.exons
    //      .map(_.sequence)
    //      .fold(g.sequence)((a, b) => a.replace(b, List.fill(b.length)(exonLabel).mkString))
    //      .replaceAll("[ACGT]", intronLabel)

    //    def gene2LabledPoints(g: Gene): Seq[LabeledPoint] = {
    //      val ExonLabel = "0"
    //      val IntronLabel = "1"
    //      g.exons.map(_.sequence)
    //        .fold(g.sequence)((a, b) => a.replace(b, List.fill(b.length)(ExonLabel).mkString))
    //        .replaceAll("[ACGT]", IntronLabel)
    //        .map(_.asDigit)
    //        .zip(g.sequence.map(n => NucleotideId(n)))
    //        .map{case (y, x) => LabeledPoint(y, Vectors.dense(x))}
    //    }

    //data.map(g => gene2LabledPoints(g)).take(10).foreach(println)


    //val Array(training, test) = data.sample(true, 0.0001).randomSplit(Array(0.6, 0.4))
    //
    //    val results = data.takeSample(true, 10)
    //    results.map { g =>
    //      markExons(g)
    //        .zip(g.sequence.map(n => NucleotideId(n)))
    //        .map{case (y, x) => LabeledPoint(y.asDigit, Vectors.dense(x.toDouble))}
    //    }.foreach(println)


    //    def lp(y: Int, x: Int) = LabeledPoint(y.toDouble, Vectors.dense(x.toDouble))
    //    val trainingSet = Seq(lp(1,1), lp(0,0), lp(0,0), lp(1,1))
    //
    //    val trainingRDD = sc.parallelize(trainingSet)
    //    trainingRDD.collect().foreach(println)
    //    val model = NaiveBayes.train(trainingRDD, 0.01)
    //    val y0 = model.predict(Vectors.dense(0.0))
    //    val y1 = model.predict(Vectors.dense(1.0))
    //    println(s"Predicted value y(0) = $y0, y(1) = $y1")
    //    sc.stop();
  }


}

