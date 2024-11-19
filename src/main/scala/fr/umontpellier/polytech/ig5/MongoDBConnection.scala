package fr.umontpellier.polytech.ig5

import com.mongodb.{ServerApi, ServerApiVersion}
import org.mongodb.scala.{ConnectionString, MongoClient, MongoClientSettings, MongoDatabase}

object MongoDBConnection {

  // Méthode pour obtenir la base de données MongoDB
  def getDatabase(dbName: String): MongoDatabase = {
    val connectionString = "";
    val serverApi = ServerApi.builder.version(ServerApiVersion.V1).build()

    val settings = MongoClientSettings
      .builder()
      .applyConnectionString(ConnectionString(connectionString))
      .serverApi(serverApi)
      .build()

    println("Connection to MongoDB established.")
    // Création du client MongoDB
    val mongoClient = MongoClient(settings)
    // Retourne la base de données spécifiée
    mongoClient.getDatabase(dbName)
  }
}