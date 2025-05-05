package reps

import reps.api.{APIClient, FingridClient}
import reps.io.FileIO
import reps.service.{AlertSystem, DataAnalysis}

import scala.io.StdIn.readLine
import scala.util.{Failure, Success}

object Main {

  def main(args: Array[String]): Unit = {
    mainLoop()
  }


  def printMenu(): Unit = {
    println(
      """
        |========= Renewable Energy Plant System =========
        |1. Control renewable energy sources
        |2. Collect and store energy data
        |3. Query and analyze data files
        |4. Check national power system status
        |5. View power plant generation data
        |0. Exit
        |===============================================
        |Please enter your choice:
        |""".stripMargin)
  }

  @annotation.tailrec
  def mainLoop(): Unit = {
    printMenu()

    readLine().trim match {
      case "1" => controlEnergySource(); mainLoop()
      case "2" => collectAndStoreData(); mainLoop()
      case "3" => queryAndAnalyzeData(); mainLoop()
      case "4" => checkSystemStatus(); mainLoop()
      case "5" => handlePlantDataView(); mainLoop()
      case "0" => println("Goodbye!")
      case _   => println("Invalid input, please try again."); mainLoop()
    }
  }


  def controlEnergySource(): Unit = {
    println("Select the source to control: 1. Solar 2. Wind 3. Hydropower")
    val input = readLine().trim

    val sources = Map(
      "1" -> "Solar panels",
      "2" -> "Wind turbines",
      "3" -> "Hydropower generators"
    )

    sources.get(input) match {
      case Some(source) =>
        println(s"Initializing control interface for $source...")
        println(s"$source are now running in auto-optimization mode.")
        println(s"Status: Simulated remote control session completed.")
      case None =>
        println("Invalid input. Please select 1, 2, or 3.")
        println("For example: enter '1' to control Solar panels.")
    }
  }

  def collectAndStoreData(): Unit = {
    println("Select the source: 1. Solar 2. Wind 3. Hydropower 4. Power Plant Total Generation")
    val src = readLine().trim

    println("Enter start date (e.g. 2025-05-01):")
    val fromDate = readLine().trim
    println("Enter end date (e.g. 2025-05-03):")
    val toDate = readLine().trim

    val (name, id, filename) = src match {
      case "1" => ("Solar", "247", "solar.csv")
      case "2" => ("Wind", "181", "wind.csv")
      case "3" => ("Hydropower", "191", "hydro.csv")
      case "4" => ("Total Power Generation", "74", "plant.csv")
      case _   => ("", "", "")
    }

    if (id.nonEmpty) {
      if (!fromDate.matches("""\d{4}-\d{2}-\d{2}""")) {
        println("Invalid start date format. Please enter the date in the format 'YYYY-MM-DD'.")
        println("For example: enter '2025-05-01' for May 1, 2025.")
        return
      }

      if (!toDate.matches("""\d{4}-\d{2}-\d{2}""")) {
        println("Invalid end date format. Please enter the date in the format 'YYYY-MM-DD'.")
        println("For example: enter '2025-05-03' for May 3, 2025.")
        return
      }

      val startOpt = Some(fromDate + "T00:00:00Z")
      val endOpt = Some(toDate + "T23:59:59Z")

      val data = APIClient.fetchAllPages(id, startTime = startOpt, endTime = endOpt)

      if (data.nonEmpty) {
        FileIO.writeToFile(filename, data)
        println(s"$name data written to $filename (${data.size} records)")
        println("Data preview:")
        data.take(1).foreach(r => println(s"${r.timestamp} -> ${r.value} kW"))
      } else {
        println(s"No $name data found for that time range.")
      }
    } else {
      println("Invalid source selection.")
    }
  }



  def queryAndAnalyzeData(): Unit = {
    println("Select data source: 1. Solar 2. Wind 3. Hydropower")
    val filename = readLine().trim match {
      case "1" => "solar.csv"
      case "2" => "wind.csv"
      case "3" => "hydro.csv"
      case _   => ""
    }

    if (filename.nonEmpty) {
      println("Enter the timestamp to query from (e.g. 2024-04-12T14:00:00):")
      val rawInput = readLine().trim
      val inputTime =
        if (rawInput.matches("""\d{4}-\d{2}-\d{2}"""))
          rawInput + "T00:00:00"
        else
          rawInput


      println("Select analysis granularity: 1. Hourly 2. Daily 3. Weekly 4. Monthly")
      val granularity = readLine().trim

      println("Sort results by average power output (highest to lowest)? (Y/N)")
      val sort = readLine().trim.toUpperCase == "Y"

      DataAnalysis.analyzeFile(filename, inputTime, granularity, sort)
    } else {
      println("Invalid source selection")
    }
  }

  def checkSystemStatus(): Unit = {
    println("Checking system status from Fingrid...")

    val result = FingridClient.getSystemStatusCode()
    result match {
      case Success(code) =>
        AlertSystem.reportSystemState(code)
      case Failure(ex) =>
        println(s"Failed to get system status: ${ex.getMessage}")
    }
  }



  def handlePlantDataView(): Unit = {
    println("Enter start date (e.g. 2025-05-01):")
    val from = readLine().trim + "T00:00:00Z" // start time

    println("Enter end date (e.g. 2025-05-03):")
    val to = readLine().trim + "T23:59:59Z"   // end time

    println("Select display granularity: 1. Hourly 2. Daily 3. Weekly 4. Monthly")
    val granularity = readLine().trim         // time granularity

    println("Sort by time descending? (Y/N)")
    val descending = readLine().trim.toUpperCase == "Y" // sort order

    DataAnalysis.viewPlantData(from, to, granularity, descending) // view data
  }


}
