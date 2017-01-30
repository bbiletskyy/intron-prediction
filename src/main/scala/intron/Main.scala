package intron

import org.apache.spark.annotation.DeveloperApi
import org.apache.spark.ml.{Pipeline, Transformer}
import org.apache.spark.ml.classification.{DecisionTreeClassifier, NaiveBayes}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql._
import org.apache.spark.sql.functions._


/**
  * Transforms genes into sliding frames, trains and predicts introns and exons using the methods:
  * - Naive Bayes
  * - Decision Tree
  */
object Main {
  //import org.apache.spark.sql.functions.udf
  val featuresIndexer = udf{xs: Seq[String] => Vectors.dense(featureIndex(xs.head), xs.tail.map(x => featureIndex(x)) :_*)}
  val labelIndexer = udf{y: String => labelIndex(y)}

  def main(args: Array[String]) {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("intron-prediction")
      .config("spark.master", "local[4]")
      .getOrCreate()
    import spark.implicits._

    val genes = Data.getValidGenes(spark.sparkContext).sample(false, 0.001).cache()
    println(s"Valid genes uploaded ${genes.count()}, unique genes among them: ${genes.distinct().count()}")

    val Array(test, train) = genes.randomSplit(Array(0.2, 0.8))
    val frameRadius = 10
    val trainFrames = train.flatMap(gene => toFrames(gene, frameRadius))
      .toDF("features", "label")
      .withColumn("indexedFeatures", featuresIndexer('features))
      .withColumn("indexedLabel", labelIndexer('label))
      .cache()
    println(s"Using ${trainFrames.count()} frames for training  (${trainFrames.distinct().count()} unique frames), frame size is ${frameRadius * 2 + 1}")

    val naiveBayes = new NaiveBayes()
      .setLabelCol("indexedLabel")
      .setFeaturesCol("indexedFeatures")
      .setPredictionCol("NB-predicted")
      .setRawPredictionCol("NB-rawPrediction")
      .setProbabilityCol("NB-probability")
    val dt = new DecisionTreeClassifier()
      .setLabelCol("indexedLabel")
      .setFeaturesCol("indexedFeatures")
      .setPredictionCol("DT-predicted")
      .setRawPredictionCol("DT-rawPrediction")
      .setProbabilityCol("DT-probability")
      .setMaxDepth(10)


    val pipeline = new Pipeline().setStages(Array(naiveBayes, dt))
    val model = pipeline.fit(trainFrames)

    val testFrames = test
      .flatMap(gene => toFrames(gene, frameRadius))
      .toDF("features", "label")
      .withColumn("indexedFeatures", featuresIndexer('features))
      .withColumn("indexedLabel", labelIndexer('label))
      .cache()

    val predictedFrames = model.transform(testFrames)
    predictedFrames
      .select("features", "indexedLabel", "NB-predicted", "DT-predicted")
      .show(truncate = false)

    val evaluatorNB = new MulticlassClassificationEvaluator()
      .setLabelCol("indexedLabel")
      .setPredictionCol("NB-predicted")
      .setMetricName("accuracy")
    val evaluatorDT = new MulticlassClassificationEvaluator()
      .setLabelCol("indexedLabel")
      .setPredictionCol("DT-predicted")
      .setMetricName("accuracy")

    val predictedFramesCount = predictedFrames.count()
    val accuracyNB = evaluatorNB.evaluate(predictedFrames)
    println(s"!!!!!!!Naive Bayes test error = ${(1.0 - accuracyNB)}"  )
    val nBCorrect = predictedFrames.filter($"indexedLabel" === $"NB-predicted")
    println(s"!!!!!!!Naive Bayes correctly predicted ${nBCorrect.count()} out of $predictedFramesCount")

    val accuracyDT = evaluatorDT.evaluate(predictedFrames)
    println("!!!!!!!Decision Tree test error = " + (1.0 - accuracyDT))
    val dTCorrect = predictedFrames.filter($"indexedLabel" === $"DT-predicted")
    println(s"!!!!!!!Decision Tree correctly predicted ${dTCorrect.count()} out of $predictedFramesCount")
    //Calculate the accuracy of exon prediction
    val predictedExonFrames = predictedFrames.filter($"label" === ExonSymbol)
    val exonAccuracyNB = evaluatorNB.evaluate(predictedExonFrames)
    println(s"!!!!!!!Naive Bayes exons test error = ${(1.0 - exonAccuracyNB)}")
    val exonAccuracyDT = evaluatorDT.evaluate(predictedExonFrames)
    println(s"!!!!!!!Decision Tree exons test error = ${(1.0 - exonAccuracyDT)}")
    spark.stop()
  }
}
