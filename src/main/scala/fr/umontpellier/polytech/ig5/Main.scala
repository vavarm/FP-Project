package fr.umontpellier.polytech.ig5

object Main extends App {

  Logger.infoLog("Application Started")

  val transferA2N = new TransferAtlas2Neo4j()
  transferA2N.transformAndPushData()
  transferA2N.closeConnections()
}
