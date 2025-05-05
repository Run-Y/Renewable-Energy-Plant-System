package reps.io

import reps.model.EnergyRecord

import java.io._
import scala.io.Source
import scala.util.Try

object FileIO {

  def writeToFile(filename: String, data: List[EnergyRecord]): Try[Unit] = Try {
    val writer = new BufferedWriter(new FileWriter(filename))
    data.foreach { record =>
      writer.write(s"${record.timestamp},${record.value}\n")
    }
    writer.close()
  }

  def readFromFile(filename: String): Try[List[EnergyRecord]] = Try {
    val source = Source.fromFile(filename)
    val records = source.getLines().toList.flatMap { line =>
      val parts = line.split(",")
      if (parts.length == 2)
        Try(EnergyRecord(parts(0), parts(1).toDouble)).toOption
      else None
    }
    source.close()
    records
  }
}
