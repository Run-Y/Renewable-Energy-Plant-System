package reps.api

import reps.model.EnergyRecord
import scala.util.Try

trait DataFetcher {
  def fetchData(sourceId: String): Try[List[EnergyRecord]]
}
