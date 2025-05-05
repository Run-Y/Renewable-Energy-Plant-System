package reps.api

import sttp.client3._
import play.api.libs.json._
import scala.util.{Try, Success, Failure}

object FingridClient {

  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend() // HTTP client backend

  // Case class for system status record
  case class SystemStatus(startTime: String, endTime: String, value: Int)
  implicit val statusReads: Reads[SystemStatus] = Json.reads[SystemStatus] // JSON reader

  // Fetch latest system status code
  def getSystemStatusCode(): Try[Int] = {
    val url = uri"https://data.fingrid.fi/api/datasets/209/data" // API endpoint
    val apiKey = "26ad4d673e9a4235b8381ce2e460cee0"              // API key

    val request = basicRequest
      .header("x-api-key", apiKey) // add API key header
      .get(url)                    // GET request
      .response(asStringAlways)    // always get response body as string

    Try {
      val response = request.send()              // send request
      val json = Json.parse(response.body)       // parse JSON

      // Extract first data value from JSON
      val maybeValue: Option[Int] = for {
        data <- (json \ "data").asOpt[JsArray]           // get "data" array
        first <- data.value.headOption                   // get first element
        value <- (first \ "value").asOpt[Int]            // extract "value"
      } yield value

      // Return value or throw error if not found
      maybeValue.getOrElse(throw new Exception("No valid status data found"))
    }
  }

}

