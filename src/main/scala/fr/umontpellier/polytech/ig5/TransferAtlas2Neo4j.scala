package fr.umontpellier.polytech.ig5

import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDocument
import org.neo4j.driver.{AuthTokens, GraphDatabase, Session}
import scala.concurrent.duration._
import scala.concurrent.Await

import scala.jdk.CollectionConverters.CollectionHasAsScala

class TransferAtlas2Neo4j() {

  // MongoDB Atlas information
  // Localhost information to test out the code
  private val mongoUri: String = ""
  private val dbName: String = ""
  private val collectionName: String = ""

  // Neo4j information
  private val neo4jUri: String = "bolt://localhost:7687"
  private val neo4jUser: String = "neo4j"
  private val neo4jPassword: String = "scalaProject" //neo4j

  // MongoDB client and database initialization
  private val mongoClient: MongoClient = MongoClient(mongoUri)
  private val database: MongoDatabase = mongoClient.getDatabase(dbName)
  private val collection: MongoCollection[BsonDocument] = database.getCollection(collectionName).withDocumentClass[BsonDocument]

  // Neo4j driver and session initialization
  private val driver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(neo4jUser, neo4jPassword))
  private val session: Session = driver.session()

  // Fetch data from MongoDB
  def fetchData(): Seq[BsonDocument] = {
    try {
      val futureResult = collection.find().toFuture()
      Await.result(futureResult, 10.seconds) // Wait for up to 10 seconds
    } catch {
      case e: Throwable =>
        println(s"Error fetching data: ${e.getMessage}")
        Seq.empty[BsonDocument]
    }
  }

  // Transform MongoDB JSON into Cypher
  def transformAndPushData(): Unit = {
    val documents = fetchData()

    documents.foreach { doc =>
      val cveId = doc.getDocument("cve").getString("id").getValue

      val relationships = doc.getDocument("cve")
        .getArray("relationships")
        .getValues
        .asScala
        .map(_.asDocument())

      relationships.foreach { relationship =>
        val relType = relationship.getString("type").getValue
        val target = relationship.getDocument("target")

        // Generate node label and properties
        val (targetLabel, targetProps) = if (target.containsKey("Description")) {
          ("Description", s"description: '${target.getString("Description").getValue}'")
        } else if (target.containsKey("ImpactScore")) {
          ("ImpactScore", s"score: ${target.getDouble("ImpactScore").getValue}")
        } else {
          ("Unknown", "")
        }

        // Push data to Neo4j
        val cypher = s"""
          MERGE (cve:CVE {id: '$cveId'})
          CREATE (target:$targetLabel { $targetProps })
          CREATE (cve)-[:$relType]->(target)
        """
        executeCypher(cypher)
      }
    }
  }

  // Execute Cypher query
  private def executeCypher(cypher: String): Unit = {
    try {
      session.run(cypher)
    } catch {
      case e: Exception => println(s"Error executing Cypher query: ${e.getMessage}")
    }
  }

  // Close connections
  def closeConnections(): Unit = {
    session.close()
    driver.close()
    mongoClient.close()
  }
}