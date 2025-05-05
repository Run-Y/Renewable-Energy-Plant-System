package reps.io

import reps.model.EnergyRecord

import java.io._
import scala.io.Source
import scala.util.Try

object FileIO {

  def writeToFile(filename: String, data: List[EnergyRecord]): Try[Unit] = Try {
    val writer = new BufferedWriter(new FileWriter(filename)) // open file for writing
    data.foreach { record =>
      writer.write(s"${record.timestamp},${record.value}\n") // write each record
    }
    writer.close() // close file
  }

  def readFromFile(filename: String): Try[List[EnergyRecord]] = Try {
    val source = Source.fromFile(filename) // open file for reading
    val records = source.getLines().toList.flatMap { line =>
      val parts = line.split(",") // split line into parts
      if (parts.length == 2)
        Try(EnergyRecord(parts(0), parts(1).toDouble)).toOption // parse record
      else None // skip bad line
    }
    source.close() // close file
    records
  }

