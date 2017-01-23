/**
 * Domain classes
 */
package object intron {

  case class Exon(exonId: String, geneId: String, sequence: String)

  case class Gene(geneId: String, exonIds: List[String] = Nil, exons: List[Exon] = Nil, sequence: String)

  case class Frame[X, Y](xs: List[X], y: Y) {
    assert((xs.size - 1) % 2 == 0)

    def radius = (xs.size - 1) / 2
  }

  def toFrames(g: Gene, frameRadius: Int): Iterator[Frame[Char, Char]] = {
    val exonSymbol = "+"
    val intronSymbol = "-"
    val ys = g.exons.map(_.sequence)
      .fold(g.sequence)((geneSeq, exonSeq) => geneSeq.replace(exonSeq, exonSymbol * exonSeq.length))
      .replaceAll(s"[^${exonSymbol}]", intronSymbol)

    val flankingSymbol = "X"
    val frameSize = frameRadius * 2 + 1
    val xs = flankingSymbol * frameRadius + g.sequence.toUpperCase + flankingSymbol * frameRadius
    xs.sliding(frameSize).zip(ys.iterator).map { case (xs, y) => Frame(xs.toList, y) }
  }
}
