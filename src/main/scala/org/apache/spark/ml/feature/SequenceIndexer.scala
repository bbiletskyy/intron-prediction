package org.apache.spark.ml.feature

import scala.collection.Map

import org.apache.hadoop.fs.Path
import org.apache.spark.SparkException
import org.apache.spark.ml._
import org.apache.spark.ml.attribute.NominalAttribute
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml.util._
import org.apache.spark.ml.linalg._
import org.apache.spark.sql._
import org.apache.spark.sql.catalyst.ScalaReflection._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.util.collection.OpenHashMap

/**
 * Indexes sequences of [[String]]. It works similar to [[StringIndexer]], except that the value of input column is
 * [[Array]] of [[String]] instead of [[String]]. We assume that all string values are coming from the same shared alphabet.
 *
 * @param uid - unique identifier of a pipeline stage
 */
class SequenceIndexer(override val uid: String)
    extends Estimator[SequenceIndexerModel]
    with SequenceIndexerBase
    with DefaultParamsWritable {

  def this() = this(Identifiable.randomUID("sequenceIndexer"))

  override def fit(dataset: Dataset[_]): SequenceIndexerModel = {
    transformSchema(dataset.schema, logging = true)
    //select the column containing sequences of labels
    val counts: Map[String, Long] = dataset.select(col($(inputCol)).cast(new ArrayType(StringType, true)))
      .rdd
      .flatMap(_.getSeq[String](0))
      .countByValue()
    //get labels sorted by counts, so that the most frequent labels get lesser indexes
    val labels = counts.toSeq.sortBy(-_._2).map(_._1).toArray
    copyValues(new SequenceIndexerModel(uid, labels).setParent(this))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): SequenceIndexer = defaultCopy(extra)
}

object SequenceIndexer extends DefaultParamsReadable[SequenceIndexer] {

  override def load(path: String): SequenceIndexer = super.load(path)
}

/**
 * Model for [[SequenceIndexer]]. It is generated as the result of [[SequenceIndexer.fit()]].
 * @param uid - unique identifier of a pipeline stage
 * @param labels - alphabel of all possibe labels
 */
class SequenceIndexerModel(override val uid: String, val labels: Array[String])
    extends Model[SequenceIndexerModel]
    with SequenceIndexerBase
    with MLWritable {

  import SequenceIndexerModel._

  def this(labels: Array[String]) = this(Identifiable.randomUID("sequenceIndexer"), labels)

  //preparing a map of label indexes, the index of each label is the corresponding index in the array
  private val label2Index: OpenHashMap[String, Double] = {
    val map = new OpenHashMap[String, Double](labels.length)
    labels.zipWithIndex.foreach { case (label, index) => map.update(label, index) }
    map
  }

  override def copy(extra: ParamMap): SequenceIndexerModel = {
    val copied = new SequenceIndexerModel(uid, labels)
    copyValues(copied, extra).setParent(parent)
  }

  override def write: MLWriter = new SequenceIndexModelWriter(this)

  override def transformSchema(schema: StructType): StructType = {
    if (schema.fieldNames.contains($(inputCol))) {
      validateAndTransformSchema(schema)
    } else {
      //if the input column doe not exist we skip schema transformation
      schema
    }
  }

  override def transform(dataset: Dataset[_]): DataFrame = {
    if (!dataset.schema.fieldNames.contains($(inputCol))) {
      logInfo(s"Input column ${$(inputCol)} does not exist during transformation. " +
        "Skip SequenceIndexerModel.")
      return dataset.toDF
    }
    transformSchema(dataset.schema, logging = true)
    val indexer: UserDefinedFunction = udf { labels: Seq[String] =>
      val indexes = labels.map { label =>
        if (label2Index.contains(label)) {
          label2Index(label)
        } else {
          throw new SparkException(s"Unseen label: $label")
        }
      }
      Vectors.dense(indexes.head, indexes.tail: _*)
    }

    val metadata = NominalAttribute.defaultAttr
      .withName($(outputCol)).withValues(labels).toMetadata()

    // If we are skipping invalid records, filter them out.
    val filteredDataset = getHandleInvalid match {
      case "skip" =>
        val filterer = udf { labels: Seq[String] =>
          labels.forall(label => label2Index.contains(label))
        }
        dataset.where(filterer(dataset($(inputCol))))
      case _ => dataset
    }
    filteredDataset.select(col("*"),
      indexer(dataset($(inputCol)).cast(new ArrayType(StringType, true))).as($(outputCol), metadata))
  }
}

private[feature] trait SequenceIndexerBase
    extends Params
    with HasInputCol
    with HasOutputCol
    with HasHandleInvalid {

  /** Validates and transforms the input schema. */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    val inputColName = $(inputCol)
    val inputDataType = schema(inputColName).dataType
    require(inputDataType == new ArrayType(StringType, true),
      s"The input column $inputColName must be either a sequence of strings type, " +
        s"but got $inputDataType.")
    val inputFields = schema.fields
    val outputColName = $(outputCol)
    //Perhaps this could be done without reflexion
    val outputColType = schemaFor[Vector].dataType
    require(inputFields.forall(_.name != outputColName),
      s"Output column $outputColName already exists.")
    val outputFields = inputFields :+ StructField(outputColName, outputColType, nullable = false)
    StructType(outputFields)
  }

  def setHandleInvalid(value: String): this.type = set(handleInvalid, value)
  setDefault(handleInvalid, "error")

  def setInputCol(value: String): this.type = set(inputCol, value)

  def setOutputCol(value: String): this.type = set(outputCol, value)

}

object SequenceIndexerModel extends MLReadable[SequenceIndexerModel] {

  private[SequenceIndexerModel] class SequenceIndexModelWriter(instance: SequenceIndexerModel) extends MLWriter {

    private case class Data(labels: Array[String])

    override protected def saveImpl(path: String): Unit = {
      DefaultParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.labels)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class SequenceIndexerModelReader extends MLReader[SequenceIndexerModel] {

    private val className = classOf[SequenceIndexerModel].getName

    override def load(path: String): SequenceIndexerModel = {
      val metadata = DefaultParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)
        .select("labels")
        .head()
      val labels = data.getAs[Seq[String]](0).toArray
      val model = new SequenceIndexerModel(metadata.uid, labels)
      DefaultParamsReader.getAndSetParams(model, metadata)
      model
    }
  }

  override def read: MLReader[SequenceIndexerModel] = new SequenceIndexerModelReader

  override def load(path: String): SequenceIndexerModel = super.load(path)
}
