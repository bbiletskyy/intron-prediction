name := """intron-prediction"""

// 2.11 doesn't seem to work
scalaVersion := "2.10.4"

libraryDependencies ++= Dependencies.sparkHadoop

releaseSettings

scalariformSettings


fork in run := true
