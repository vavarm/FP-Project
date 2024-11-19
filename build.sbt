ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

lazy val root = (project in file("."))
  .settings(
    name := "FP-Project",

    libraryDependencies ++= Seq(
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.9.0",
      "org.neo4j.driver" % "neo4j-java-driver" % "5.22.0",
      "org.neo4j" %% "neo4j-connector-apache-spark" % "5.3.1_for_spark_3",
      "org.apache.spark" %% "spark-sql" % "3.5.3",
      "com.lihaoyi" %% "ujson" % "4.0.0",
      "io.circe" %% "circe-core" % "0.14.7",
      "io.circe" %% "circe-generic" % "0.14.9",
      "io.circe" %% "circe-parser" % "0.14.9"
    )
  )
