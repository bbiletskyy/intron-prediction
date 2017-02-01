package intron

import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.linalg._
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util._
import org.apache.spark.sql.types._

/**
 * Indexes elements of nucleotide sequnces. It uses [[featureIndex]] as a dictionary.
 * TODO: Add fitting logic, so that the dictionary is automatically vreated during the fitting stage.
 * @param uid - unique identifier of a transformer
 * @param dictionary - a map containing indexes for each element of the alphabet.
 */
class SequenceIndexer(override val uid: String, dictionary: Map[String, Double] = featureIndex)
    extends UnaryTransformer[Seq[String], Vector, SequenceIndexer] with DefaultParamsWritable {
  def this() = this(Identifiable.randomUID("sequence indexer"))
  override protected def createTransformFunc: Seq[String] => Vector = { xs =>
    Vectors.dense(dictionary(xs.head), xs.tail.map(dictionary(_)): _*)
  }
  override protected def validateInputType(inputType: DataType): Unit = {
    require(inputType == new ArrayType(StringType, true), s"Input type must be string type but got $inputType.")
  }
  override protected def outputDataType: DataType = {
    import org.apache.spark.sql.catalyst.ScalaReflection.schemaFor
    schemaFor[Vector].dataType
  }
  override def copy(extra: ParamMap): SequenceIndexer = defaultCopy(extra)
}
