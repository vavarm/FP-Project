package fr.umontpellier.polytech.ig5

object Logger {

  // Define ANSI color codes
  private val GREEN = "\u001b[32m"
  private val RED = "\u001b[31m"
  private val RESET = "\u001b[0m"

  private def log(level: String, color: String)(message: String): Unit = {
    println(s"$color[$level]$RESET $message")
  }

  val infoLog: String => Unit = log("INFO", GREEN)
  val errorLog: String => Unit = log("ERROR", RED)

}