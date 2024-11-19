package fr.umontpellier.polytech.ig5

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object CVEExtractor extends App {

  Logger.getLogger("org").setLevel(Level.ERROR)


  val spark = SparkSession.builder()
    .appName("CVE JSON Extractor")
    .config("spark.driver.memory", "8G")
    .master("local[*]")
    .getOrCreate()


  val jsonFolderPath = "data/all-cve"

  // Charge tous les fichiers JSON dans un DataFrame
  val rawDF = spark.read
    .option("multiline", "true")
    .json(s"$jsonFolderPath/*.json")

  rawDF.printSchema()  // Affiche la structure du DataFrame

  // Extrait les données pertinentes et les renomme
  val extractedDF = rawDF
    .select(
      explode(col("CVE_Items")).as("item")
    )
    .select(
      col("item.cve.CVE_data_meta.ID").alias("ID"),
      col("item.cve.description.description_data")(0)("value").alias("Description"),
      col("item.impact.baseMetricV3.cvssV3.baseScore").alias("baseScore"),
      col("item.impact.baseMetricV3.cvssV3.baseSeverity").alias("baseSeverity"),
      col("item.impact.baseMetricV3.exploitabilityScore").alias("exploitabilityScore"),
      col("item.impact.baseMetricV3.impactScore").alias("impactScore")
    )

  // Sauvegarde les données extraites dans un fichier JSON unique
  extractedDF
    .coalesce(1)  // Fusionne les partitions en une seule
    .write
    .mode("overwrite")
    .json("data/extracted_cves.json")

  println("Extraction terminée. Les données sont enregistrées dans 'data/extracted_cves.json'.")
}