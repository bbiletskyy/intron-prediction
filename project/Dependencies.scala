import sbt._

object Version {
  val spark        = "2.1.0"
  val hadoop       = "2.7.3"
  val slf4j        = "1.7.21"
  //val logback      = "1.1.7"
  val scalaTest    = "3.0.1"
}

object Library {
  val sparkCore      = "org.apache.spark"  %% "spark-core"      % Version.spark
  val sparkSql       = "org.apache.spark"  %% "spark-sql"       % Version.spark
  val sparkMlLib     = "org.apache.spark"  %% "spark-mllib"     % Version.spark
  val hadoopClient   = "org.apache.hadoop" %  "hadoop-client"   % Version.hadoop
  val slf4jApi       = "org.slf4j"         %  "slf4j-api"       % Version.slf4j
  val scalaTest      = "org.scalatest"     %% "scalatest"       % Version.scalaTest
}

object Dependencies {
  import Library._

  val sparkHadoop = Seq(
    sparkCore
      .exclude("org.scala-lang" , "scala-compiler")
      .exclude("org.scala-lang", "scala-reflect")
      .exclude("com.google.guava", "guava")
      .exclude("org.scalatest", "scalatest_2.11"),
    sparkMlLib,
    hadoopClient,
    "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.6",
    scalaTest % "test"
  )
}
