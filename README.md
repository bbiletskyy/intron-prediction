# Intron Prediction

## Description
Distributed Machile Learning System for intron-exon predictioin in human DNA.

## How to run
Application depends on 2 data files (see below in `Data` section).
- `sbt run`

## How to test
- `sbt test`

## Data

Application depends on 2 data files located in the `./data/` folder.
- `exons.txt`
- `genes.txt`

See below how can you obtain them.

### Dummy Test Data
- `/src/test/resources/data/exons.sample`
- `/src/test/resources/data/genes.sample`

### Data Download
You can download and decompress data files:
- human genome exons [exons.txt](https://drive.google.com/file/d/0BzlYsyqnvqi3MllabFYzckVCdmc/view?usp=sharing)
- human genome genes [genes.txt](https://drive.google.com/file/d/0BzlYsyqnvqi3SVpTNmEydTYyaDQ/view?usp=sharing)

### Data Generation
You can generate data from eENSEMBL files yourself:

#### Generating `exons.txt` file

1. Navigate to http://www.ensembl.org/biomart/martview/
2. In `- CHOOSE DATABASE -` select `Ensembl Genes 86`
3. In `- CHOOSE DATASET - ` select `Homo sapiens genes (GRCh38.p7)`
4. Click `Attributes`
5. Select `Sequences`
6. Expand  `Heder Informations` end select the following attributes, and unselect all the rest:
  1. Ensembl Gene ID
  2. Ensembl Exon ID
7. Expand  `Sequences` and select `Exon sequences`, so that the left panel of attributes looks like (preserving the order):
  - Gene ID
  - Exon ID
  - Exon sequences
8. Click `Results`
9. Select `Compressed file (.gz)`
10. Click `Go`
11. Rename the downloaded file to `exons.txt`

#### Generating `genes.txt` file

1. Navigate to http://www.ensembl.org/biomart/martview/
2. In `- CHOOSE DATABASE -` select `Ensembl Genes 86`
3. In `- CHOOSE DATASET - ` select `Homo sapiens genes (GRCh38.p7)`
4. Click `Attributes`
5. Select `Sequences`
6. Expand  `Heder Informations` end select the following attributes, and unselect all the rest:
  1. Ensembl Gene ID
  2. Ensembl Exon ID
7. Expand  `Sequences` and select `Unspliced (Gene)`, so that the left panel of attributes looks like (preserving the order):
  - Gene ID
  - Exon ID
  - Unspliced (Gene)
9. Click `Filters` on the left panel
10. Expand `Region`
11. Check `Chromosome/scaffold`
12. Select `1` from the multi-select list
13. Click `Results`
14. Select `Compressed file (.gz)`
15. Click `Go`
16. Rename the downloaded file to `genes-1.txt`
17. Perform the above steps for chromosomes `1` to `22` as well as for `X` and `Y` chromosomes
18. Concatenate 24 resulting files into a single `genes.txt` file



