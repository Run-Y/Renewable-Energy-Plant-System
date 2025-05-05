package reps.api

import reps.model.EnergyRecord
import sttp.client3._
import play.api.libs.json._
import scala.util.{Try, Success, Failure}

object APIClient extends DataFetcher {

  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend() // HTTP client backend
  val apiKey = "26ad4d673e9a4235b8381ce2e460cee0" // API key for Fingrid

  case class RawRecord(startTime: String, endTime: String, value: Double) // raw API data structure

  implicit val rawRecordReads: Reads[RawRecord] = Json.reads[RawRecord] // JSON parser for RawRecord

  // Main method to fetch all data as EnergyRecord list
  def fetchData(datasetId: String): Try[List[EnergyRecord]] = Try {
    fetchAllPages(datasetId)
  }

  // Recursive method to fetch paginated API data
  def fetchAllPages(
                     datasetId: String,
                     page: Int = 1,
                     acc: List[EnergyRecord] = Nil,
                     startTime: Option[String] = None,
                     endTime: Option[String] = None
                   ): List[EnergyRecord] = {
    println("Reading...")

    // Build base and parameterized URL
    val baseUri = uri"https://data.fingrid.fi/api/datasets/$datasetId/data"
    val uriWithParams = uri"$baseUri?page=$page&pageSize=20000&format=json"
      .addParam("startTime", startTime.getOrElse("")) // add optional startTime
      .addParam("endTime", endTime.getOrElse(""))     // add optional endTime

    // Prepare and send HTTP GET request
    val request = basicRequest
      .header("x-api-key", apiKey)
      .get(uriWithParams)
      .response(asStringAlways)

    val response = request.send() // send the request
    val json = Json.parse(response.body) // parse response to JSON

    // Extract data list from JSON
    val rawList = (json \ "data").asOpt[List[RawRecord]].getOrElse(Nil)
    val newRecords = rawList.map(r => EnergyRecord(r.startTime, r.value)) // convert to EnergyRecord
    val allRecords = acc ++ newRecords // accumulate records

    // Check if more pages exist
    val nextPageOpt = (json \ "pagination" \ "nextPage").asOpt[Int]

    // Recursively fetch next page or return all records
    nextPageOpt match {
      case Some(nextPage) => fetchAllPages(datasetId, nextPage, allRecords, startTime, endTime)
      case None => allRecords
    }
  }
}



