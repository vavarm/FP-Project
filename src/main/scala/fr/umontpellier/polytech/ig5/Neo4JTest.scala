package fr.umontpellier.polytech.ig5

import org.apache.spark.sql.{ SaveMode, SparkSession, functions => F}

object Neo4jTest {
  def main(args: Array[String]): Unit = {
    // Configuration de la connexion Neo4j
    val url = "neo4j://localhost:7687"
    val username = "neo4j"
    val password = "password"
    val dbname = "neo4j"

    val spark = SparkSession.builder
      .config("neo4j.url", url)
      .config("neo4j.authentication.basic.username", username)
      .config("neo4j.authentication.basic.password", password)
      .config("neo4j.database", dbname)
      .appName("Spark App")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Chargement des données JSONL
    val data = spark.read.json("data/sample.jsonl")

    // Extraction des noeuds CVE
    val cveDF = data.select($"cve.id".as("cve_id"))
      .distinct()

    // Création des noeuds CVE dans Neo4j
    cveDF.write
      .format("org.neo4j.spark.DataSource")
      .mode(SaveMode.Overwrite)
      .option("labels", "CVE")
      .option("node.keys", "cve_id")
      .save()

    // Extraction des noeuds Description et ImpactScore avec leurs relations respectives
    val relationsDF = data.select($"cve.id".as("cve_id"), F.explode($"relations").as("relation"))
      .select(
        $"cve_id",
        $"relation.target.type".as("target_type"),
        $"relation.target.value".as("target_value")
      )

    // Filtrage et insertion des noeuds Description
    val descriptionDF = relationsDF.filter($"target_type" === "Description")
      .select($"target_value".as("description"), $"cve_id")

    descriptionDF.write
      .format("org.neo4j.spark.DataSource")
      .mode(SaveMode.Overwrite)
      .option("labels", "Description")
      .option("node.keys", "description")
      .save()

    // Filtrage et insertion des noeuds ImpactScore
    val impactScoreDF = relationsDF.filter($"target_type" === "ImpactScore")
      .select($"target_value".as("impact_score"), $"cve_id")

    impactScoreDF.write
      .format("org.neo4j.spark.DataSource")
      .mode(SaveMode.Overwrite)
      .option("labels", "ImpactScore")
      .option("node.keys", "impact_score")
      .save()

    // Création des relations entre CVE et Description
    val cveToDescriptionDF = descriptionDF.select($"cve_id", $"description")
    cveToDescriptionDF.write
      .format("org.neo4j.spark.DataSource")
      .mode(SaveMode.Append)
      .option("relationship", "HAS")
      .option("relationship.source.labels", "CVE")
      .option("relationship.source.node.keys", "cve_id")
      .option("relationship.target.labels", "Description")
      .option("relationship.target.node.keys", "description")
      .save()

    // Création des relations entre CVE et ImpactScore
    val cveToImpactScoreDF = impactScoreDF.select($"cve_id", $"impact_score")
    cveToImpactScoreDF.write
      .format("org.neo4j.spark.DataSource")
      .mode(SaveMode.Append)
      .option("relationship", "HAS")
      .option("relationship.source.labels", "CVE")
      .option("relationship.source.node.keys", "cve_id")
      .option("relationship.target.labels", "ImpactScore")
      .option("relationship.target.node.keys", "impact_score")
      .save()

    spark.stop()
  }
}