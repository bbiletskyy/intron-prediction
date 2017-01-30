/**
 * Domain classes and some commonly used routines.
 */
package object intron {
  case class Exon(exonId: String, geneId: String, sequence: String)
  case class Gene(geneId: String, exonIds: List[String] = Nil, exons: List[Exon] = Nil, sequence: String)
  case class Frame[X, Y](xs: List[X], y: Y) {
    assert((xs.size - 1) % 2 == 0)
    def radius = (xs.size - 1) / 2
  }

  val ExonSymbol = "+"
  val IntronSymbol = "-"
  val FlankingSymbol = "X"

  def toFrames(g: Gene, frameRadius: Int): Iterator[Frame[String, String]] = {
    val ys = g.exons.map(_.sequence)
      .fold(g.sequence)((geneSeq, exonSeq) => geneSeq.replace(exonSeq, ExonSymbol * exonSeq.length))
      .replaceAll(s"[^${ExonSymbol}]", IntronSymbol)
    val frameSize = frameRadius * 2 + 1
    val xs = FlankingSymbol * frameRadius + g.sequence.toUpperCase + FlankingSymbol * frameRadius
    xs.sliding(frameSize).zip(ys.iterator).map { case (xs, y) => Frame(xs.map(_.toString).toList, y.toString) }
  }

  val featureIndex: Map[String, Double] = Map(
  "A" -> 0.0,
  "C" -> 1.0,
  "G" -> 2.0,
  "T" -> 3.0,
  "N" -> 4.0,
  "F" -> 5.0,
  FlankingSymbol -> 6.0
  ).withDefaultValue(-1.0)

  val labelIndex: Map[String, Double] = Map(
    IntronSymbol -> 0.0,
    ExonSymbol -> 1.0
  )
}
