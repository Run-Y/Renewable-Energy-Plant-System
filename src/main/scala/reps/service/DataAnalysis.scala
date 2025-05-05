package reps.service

import reps.io.FileIO
import reps.model.EnergyRecord

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.{Try, Success, Failure}
import java.time.{LocalDateTime, OffsetDateTime}



object DataAnalysis {
  val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  def analyzeFile(filename: String, fromTime: String, granularity: String, sort: Boolean): Unit = {
    FileIO.readFromFile(filename) match {
      case Success(records) =>
        val fromDateTimeTry = Try(LocalDateTime.parse(fromTime, formatter))
        if (fromDateTimeTry.isFailure) {
          println("Invalid date format. Please enter the date in the format 'YYYY-MM-DD'.")
          println("For example, enter '2024-04-12' for April 12, 2024.")
          return
        }
        val fromDateTime = fromDateTimeTry.get

        val filtered = records.filter(r => isAfterTime(r.timestamp, fromDateTime))

        parseGranularity(granularity) match {
          case None =>
            println("Invalid input.")
          case Some(mode) =>
            val grouped = groupBy(filtered, mode)
            val result = grouped.map { case (k, list) =>
              val values = list.map(_.value)
              val mean = values.sum / values.size
              val sorted = values.sorted
              val median = if (values.size % 2 == 0)
                (sorted(values.size / 2 - 1) + sorted(values.size / 2)) / 2
              else
                sorted(values.size / 2)
              val mode = values.groupBy(identity).maxBy(_._2.size)._1
              val range = values.max - values.min
              val midrange = (values.max + values.min) / 2
              (k, (mean, median, mode, range, midrange))
            }.toList

            val finalResult = if (sort) result.sortBy(-_._2._1) else result

            if (finalResult.isEmpty) {
              println("No data available for the selected time range and granularity.")
            } else {
              println("Analysis Result:")
              finalResult.foreach { case (time, (mean, median, mode, range, midrange)) =>
                println(s"$time ->")
                println(f"  Mean: $mean%.2f kW")
                println(f"  Median: $median%.2f kW")
                println(f"  Mode: $mode%.2f kW")
                println(f"  Range: $range%.2f kW")
                println(f"  Midrange: $midrange%.2f kW")
              }
            }
        }

      case Failure(e) =>
        println(s"Failed to read file: ${e.getMessage}")
    }
  }

  def isAfterTime(timestamp: String, from: LocalDateTime): Boolean = {
    Try(OffsetDateTime.parse(timestamp).toLocalDateTime).toOption.exists { ts =>
      !ts.isBefore(from)
    }
  }

  def parseGranularity(input: String): Option[String] = input match {
    case "1" => Some("hour")
    case "2" => Some("day")
    case "3" => Some("week")
    case "4" => Some("month")
    case _   => None
  }

  def isInRange(timestamp: String, from: LocalDateTime, to: LocalDateTime): Boolean = {
    Try(OffsetDateTime.parse(timestamp).toLocalDateTime).toOption.exists { dt =>
      !dt.isBefore(from) && !dt.isAfter(to)
    }
  }



  def groupBy(records: List[EnergyRecord], mode: String): Map[String, List[EnergyRecord]] = {
    records.groupBy { r =>
      val dt = OffsetDateTime.parse(r.timestamp).toLocalDateTime
      mode match {
        case "hour"  => dt.withMinute(0).withSecond(0).withNano(0).toString
        case "day"   => dt.toLocalDate.toString
        case "week"  => f"${dt.getYear}-W${dt.getDayOfYear / 7}"
        case "month" => f"${dt.getYear}-${dt.getMonthValue}%02d"
        case _       => "unknown"
      }
    }
  }

  def viewPlantData(from: String, to: String, granularity: String, descending: Boolean): Unit = {


    FileIO.readFromFile("plant.csv") match {
      case Success(records) =>
        val fromDT = Try(OffsetDateTime.parse(from).toLocalDateTime).getOrElse {
          println("Invalid start date format. Please enter the date in the format 'YYYY-MM-DD'.")
          println("For example: enter '2025-05-01' for May 1, 2025.")
          return
        }

        val toDT = Try(OffsetDateTime.parse(to).toLocalDateTime).getOrElse {
          println("Invalid end date format. Please enter the date in the format 'YYYY-MM-DD'.")
          println("For example: enter '2025-05-03' for May 3, 2025.")
          return
        }

        println(s"Displaying grouped plant.csv data from $from to $to")

        val filtered = records.filter(r => isInRange(r.timestamp, fromDT, toDT))

        if (filtered.isEmpty) {
          println("No data found in the selected time range.")
        } else {
          parseGranularity(granularity) match {
            case Some(mode) =>
              val grouped = groupBy(filtered, mode)
              val sortedGroups = grouped.toList.sortBy(_._1)
              val ordered = if (descending) sortedGroups.reverse else sortedGroups

              println(f"${"Time Group"}%-20s ${"Avg (kW)"}")
              println("-" * 35)

              ordered.foreach { case (groupKey, groupData) =>
                val avg = groupData.map(_.value).sum / groupData.size
                println(f"$groupKey%-20s $avg%.2f")
              }

              println(s"\nTotal groups displayed: ${ordered.size}")

            case None =>
              println("Invalid input.")
          }
        }

      case Failure(e) =>
        println(s"Failed to read plant.csv: ${e.getMessage}")
    }
  }

}
