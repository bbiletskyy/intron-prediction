# Intron Prediction

## Description
Distributed Machile Learning System for intron-exon predictioin.

## How to run
- `sbt run`

## How to test
- `sbt test`

## Data

Application depends on 2 data files
- `exons.txt`
- `genes.txt`

### Dummy Data
Dummy data is stored in `/src/test/resources/data/exons.sample` and `/src/test/resources/data/genes.sample`

### Data Download
You can download and extracted files:
- human genome exon sequences with metadata [exons.txt](https://drive.google.com/file/d/0BzlYsyqnvqi3MllabFYzckVCdmc/view?usp=sharing)
- human genome gene sequences with metadata [genes.txt](https://drive.google.com/file/d/0BzlYsyqnvqi3SVpTNmEydTYyaDQ/view?usp=sharing)


### Data Generation
You can generate data files yourself:

1. Navigate to http://www.ensembl.org/biomart/martview/
2. In `- CHOOSE DATABASE -` select `Ensembl Genes 86`
3. In `- CHOOSE DATASET - ` select `Homo sapiens genes (GRCh38.p7)`
4. Click `Attributes`
5. Select `Sequences`
6. Expand  `Sequences` and select `Unspliced (Gene)` or `Exon sequences`
7. Expand  `Heder Informations` end select the following attributes preserving the order:
  1. Ensembl Gene ID
  2. Chromosome Name
  3. Gene Start (bp)
  4. Gene End (bp)
  5. Ensembl Transcript ID
  6. 5‘ UTR Start
  7. 5‘ UTR End
  8. 3‘ UTR Start
  9. 3‘ UTR End
  10. Transcript Start (bp)
  11. Transcript End (bp)
  12. Ensembl Exon ID
  13. Exon Chr Start (bp)
  14. Exon Chr End (bp)
  15. Exon sequences (for `exons`) / Unspliced (Gene) (for `genes`)



