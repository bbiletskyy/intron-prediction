package intron

import org.scalatest.{ FlatSpec, Matchers }

class FrameSpec extends FlatSpec with Matchers {
  val e1 = Exon("ENSE00002188145", "ENSG00000254545", "TTTGCATAGAGCGCGGAGGCTGCGGCGCTCCCAGGCCATTTCGAGCCGGAAAAGTTCCCGAAGGAGCCCCGCTCCCTCTCCCGGTTCAACTGTCCAGCCCCCTCTCCCTACCCCTCACCCGCACCCCACCCGCATCCTACCCCGCCCCCGCTTCCAACTCCTTGCGCTAAGTTCCCGCACCTCGCCCCACCCCCCGCTTTGACTCCGGTCCTGCCAGCCCCGACCTCCATCCCAAGGCCGAGGGCCCCCTCTGCTGTCGTCCGCGGGTAGGGCAGCCGCAGAGCTCATGGGGCAGAGAAGAAGGGCTCCCCCAACCGACCATCGCCCCACCCCTTCCAGTCCAGCAGGGGCCAGG")
  val e2 = Exon("ENSE00002200334", "ENSG00000254545", "GAGACCATCTTTTGAACCTTCCCCAACCAGGGGCTGGACCCTGCTGTGTCTGGAAGCCCTTCCTTGGGGAGCAGCACAGCCAGCAACAGACTTGAGTGAGTGACCTCCAGACCTGGGGAAAGGAGAGGGGAGACAGGAAGAGCCCAAAGGCCTGCGGGGAAGAACTGTGTTCAGCTTAGCTG")
  val g = Gene("ENSG00000254545", List("ENSE00002188145", "ENSE00002200334"), List(e1, e2), "TTTGCATAGAGCGCGGAGGCTGCGGCGCTCCCAGGCCATTTCGAGCCGGAAAAGTTCCCGAAGGAGCCCCGCTCCCTCTCCCGGTTCAACTGTCCAGCCCCCTCTCCCTACCCCTCACCCGCACCCCACCCGCATCCTACCCCGCCCCCGCTTCCAACTCCTTGCGCTAAGTTCCCGCACCTCGCCCCACCCCCCGCTTTGACTCCGGTCCTGCCAGCCCCGACCTCCATCCCAAGGCCGAGGGCCCCCTCTGCTGTCGTCCGCGGGTAGGGCAGCCGCAGAGCTCATGGGGCAGAGAAGAAGGGCTCCCCCAACCGACCATCGCCCCACCCCTTCCAGTCCAGCAGGGGCCAGGGTGAGGCCTGTACATGGGTGGACTTTGCGAAGGGGTGAGCAAAGAGCGGGTAGGGCCCTTCTGGCCCCGGGGCGAAGGGTGGGGTTCTAGAGAGGTTCTCTCCTGGAAGCCAGGCCCCTTTCCCCACGCTGGGTTGGTTGGCCCTGGCAGCTGGTTCCCCCATCAGTCCGCAGATGCAGCTCTCTCGAGGTACACCCCACATGAAACGCTTGACAGTTGCAGTTGCTGTTAGAAGCTTGTTCTAGGCCCAAGGCAAGGCAGCACCGTCCAACCCTAATACCAGTCTCCAACCCCACCCTGAATGCCCACCACCAAGGCTAAGACCTTTGACATTTTCTAACACCAGTCTCTGGAATCTGGCAAGAGATACAGAGAGGAGTCACCCAATCTGGATCTCCTGACTTCCTCAACAGAGACCTACACACAGGCAGGCCATGAAACTCACACAGATACTCTCACATGCTTGCAGGCACACTAGTGCACAGAGATCCCTGAGAAGCACATAGACAGAGACTCGGAGGGAATATGGTGAAGGTATGTACTATCCTTCCCCATCTAGTCAAAGGAAAAAAAAATCTTAGTATTAACTGAAAAATTTAAAGTCCACTGCAACTTGCTTATCTTTTCCTCTCTAGCAATCTCTTGATGTCTGATGTCTGTGACCCTCCAAAAGGCTCTGTCCATTTCTCTCTCCACCCCCAACCTCGGTCCTACTTTCTTTCCCACCACCACCTCTTCCTCCTGGTTCAGAATACTTGCAAAATGCTTCCAGAAACCACCGTGGCAATAGCTGTGCCCTGGGGCGCAGTGATGGTTCAGGACCTCAGACCAGCAATGAGCAAGGCTGCACATTTGTGCACAAGACACTCACTGACACACCCACTCAACCCACATCTGACCAGGACCAGGGGCTCTCCTGACCAGCTAGCAGATGCAATCCCAATTTTACCAAATTCTTTATTGAACAAAAAATCAACAGCAAACATTGTCAAACAGGAAGTTCAAACAGGGCAGGTGGGTAGGGCTGACCATCCTCTCCTTGCAGTCCCACCTCTCTCTGGAACAGGCTTCAACACTAGTGAGGGCATCAAAACCCCTGTTCTGGTGGGTAAGGAGGGTCAGGCCTTTCTAGGGAAGGGACACTCAGAGCCAGCTTCATCTGTAGGCCCCAAAGAGGAGTCACGGGCTGGGCCAGGGGGCTCTGGAGCTGCAAGGGCGCTAGACAAACGCTGCAGAGGGCGGGGGATGGAGCTCTTGGTCTGGGGGCACAGTAAGGCTTGGAGCTGGTGACTCATGGTCGCCAGGGATTCGAGGTGCCGGAGCAGGGCTTGGTGGGGACAGGAGTCCCTGTGGAAGTGCTGGCCACGGCCAAAGCCATGACCAGCTGGTGCTACTGAATAGGGATGCTGGGACTGGCGCAAGCCTGGCTCTGGGGGCCACTGCCCTCGCCCAGGATGCTGCCAGTTTTCAGGCTCTGGCCAGAGCCTCCCCCTGCCTCTGGGGGGCCTCTGCCAAGCAGAGATTCCCCTTGGAGCTATACTGCCCCTGCCCTGGCTCTGCTGCCAGGCACCTCCTGGGGGAGGGGTGCCCTTCAGGGGCACATTCGGCCTCCTGTCATCTGGCTGATAGTATCTCTTCTCCACCTTACTGTTGAAGGAGACCATCTTTTGAACCTTCCCCAACCAGGGGCTGGACCCTGCTGTGTCTGGAAGCCCTTCCTTGGGGAGCAGCACAGCCAGCAACAGACTTGAGTGAGTGACCTCCAGACCTGGGGAAAGGAGAGGGGAGACAGGAAGAGCCCAAAGGCCTGCGGGGAAGAACTGTGTTCAGCTTAGCTG")

  "Gene" should "should decompose into frames" in {
    val frameRadius = 3
    val frames = toFrames(g, frameRadius)
    frames.size shouldBe g.sequence.length
    frames.foreach(f => f.radius == frameRadius)
  }
}