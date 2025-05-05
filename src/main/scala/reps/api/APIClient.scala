package reps.api

import reps.model.EnergyRecord
import sttp.client3._
import play.api.libs.json._
import scala.util.{Try, Success, Failure}

object APIClient extends DataFetcher {

  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()
  val apiKey = "26ad4d673e9a4235b8381ce2e460cee0"

  case class RawRecord(startTime: String, endTime: String, value: Double)

  implicit val rawRecordReads: Reads[RawRecord] = Json.reads[RawRecord]

  def fetchData(datasetId: String): Try[List[EnergyRecord]] = Try {
    fetchAllPages(datasetId)
  }

  def fetchAllPages(
                     datasetId: String,
                     page: Int = 1,
                     acc: List[EnergyRecord] = Nil,
                     startTime: Option[String] = None,
                     endTime: Option[String] = None
                   ): List[EnergyRecord] = {
    println("Reading...")
    val baseUri = uri"https://data.fingrid.fi/api/datasets/$datasetId/data"
    val uriWithParams = uri"$baseUri?page=$page&pageSize=20000&format=json"
      .addParam("startTime", startTime.getOrElse(""))
      .addParam("endTime", endTime.getOrElse(""))

    val request = basicRequest
      .header("x-api-key", apiKey)
      .get(uriWithParams)
      .response(asStringAlways)

    val response = request.send()
    val json = Json.parse(response.body)

    val rawList = (json \ "data").asOpt[List[RawRecord]].getOrElse(Nil)
    val newRecords = rawList.map(r => EnergyRecord(r.startTime, r.value))
    val allRecords = acc ++ newRecords

    val nextPageOpt = (json \ "pagination" \ "nextPage").asOpt[Int]


    nextPageOpt match {
      case Some(nextPage) => fetchAllPages(datasetId, nextPage, allRecords, startTime, endTime)
      case None => allRecords
    }
  }
}


