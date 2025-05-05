package reps.api

import sttp.client3._
import play.api.libs.json._
import scala.util.{Try, Success, Failure}

object FingridClient {

  implicit val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  case class SystemStatus(startTime: String, endTime: String, value: Int)
  implicit val statusReads: Reads[SystemStatus] = Json.reads[SystemStatus]

  def getSystemStatusCode(): Try[Int] = {
    val url = uri"https://data.fingrid.fi/api/datasets/209/data"
    val apiKey = "26ad4d673e9a4235b8381ce2e460cee0"

    val request = basicRequest
      .header("x-api-key", apiKey)
      .get(url)
      .response(asStringAlways)

    Try {
      val response = request.send()
      val json = Json.parse(response.body)

      val maybeValue: Option[Int] = for {
        data <- (json \ "data").asOpt[JsArray]
        first <- data.value.headOption
        value <- (first \ "value").asOpt[Int]
      } yield value

      maybeValue.getOrElse(throw new Exception("No valid status data found"))
    }
  }


}
