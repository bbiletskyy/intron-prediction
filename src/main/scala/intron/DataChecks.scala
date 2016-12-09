package intron

import intron.Data.Gene
import org.apache.spark.rdd.RDD

/** A collection of various data consistency checks. */
object DataChecks {
  def showCorruptedData(genes: RDD[Gene]): Unit = {
    val genesWithMissingExons = genes.filter(g => g.exons.map(_.exonId).toSet != g.exonIds.toSet).collect()
    println(s"Genes with missing exons detected: ${genesWithMissingExons.length}")
    genesWithMissingExons.foreach(println)
    val genesWithNonSubstringExons = genes.filter(gene => !gene.exons.forall(exon => gene.sequence.contains(exon.sequence))).collect()
    println(s"Genes with non-substring exons detected: ${genesWithNonSubstringExons.length}")
    genesWithNonSubstringExons.foreach(println)
  }

  def refine(genes: RDD[Gene]): RDD[Gene] = {
    genes
      .filter(gene => gene.exons.map(_.exonId).toSet == gene.exonIds.toSet)
      .filter(gene => gene.exons.forall(exon => gene.sequence.contains(exon.sequence)))
  }
}
