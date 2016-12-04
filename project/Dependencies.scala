import sbt._

object Version {
  val spark        = "2.0.2"
  val hadoop       = "2.7.3"
  val slf4j        = "1.7.21"
  val logback      = "1.1.7"
  val scalaTest    = "3.0.1"
  val specs2       = "3.8.5"
  //val mockito      = "2.1.0"
}

object Library {
  val sparkCore      = "org.apache.spark"  %% "spark-core"      % Version.spark
  val hadoopClient   = "org.apache.hadoop" %  "hadoop-client"   % Version.hadoop
  val slf4jApi       = "org.slf4j"         %  "slf4j-api"       % Version.slf4j
  val logbackClassic = "ch.qos.logback"    %  "logback-classic" % Version.logback
  val scalaTest      = "org.scalatest"     %% "scalatest"       % Version.scalaTest
  //val specs2         = "org.specs2"        %% "specs2-core"      % Version.specs2
  //val mockitoAll     = "org.mockito"       %  "mockito-all"     % Version.mockito
}

object Dependencies {

  import Library._

  val sparkHadoop = Seq(
    sparkCore,
    hadoopClient,
    logbackClassic,
    scalaTest % "test"
    //specs2 % "test"
    //mockitoAll % "test"
  ) 
}
