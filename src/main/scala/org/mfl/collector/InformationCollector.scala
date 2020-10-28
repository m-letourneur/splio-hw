package org.mfl.collector

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.mfl.models.{
  ApiResponseWithAllStationInformationData,
  StationInformation
}
import org.mfl.utils.JsonSupport

import scala.concurrent.{ExecutionContext, Future}

/**
  * Connector to the Velib' `Station Information` endpoint.
  *
  * Fetch, deserialize/load to case class and filter Stations based on search
  * area predicate.
  */
class InformationCollector extends JsonSupport {

  val uri = "https://velib-metropole-opendata.smoove" +
    ".pro/opendata/Velib_Metropole/station_information.json"
  val request: HttpRequest = HttpRequest(uri = uri)

  // Splio HQ coordinates
  val refLat = 48.870983
  val refLon = 2.335390

  /**
	 * Compute distance between 2 GPS points using Haversine formula
	 *
	 * @param refLat , ref point lat in degree
	 * @param refLon , ref point lon in degree
	 * @param lat    , lat in degree
	 * @param lon    , lon in degree
	 * @return Double, distance in km
	 */
  def computeDistanceToRef(
      refLat: Double,
      refLon: Double
  )(lat: Double, lon: Double) = {
    val refLatRad = Math.toRadians(refLat)
    val refLonRad = Math.toRadians(refLon)
    val latRad    = Math.toRadians(lat)
    val lonRad    = Math.toRadians(lon)

    val deltaLat = refLatRad - latRad
    val deltaLon = refLonRad - lonRad

    val a = Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(refLatRad) *
      Math.cos(latRad) * Math
      .pow(Math.sin(deltaLon / 2), 2)
    val c      = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val earthR = 6371

    earthR * c

  }

  /**
	 * Predicate to filter based on distance to Splio HQ.
	 * True if station is located within the 1-km circle aroung Splio HQ
	 *
	 * @param stationInfo : StationInformation
	 * @return Boolean, True if distance to HQ <= 1km
	 */
  def isWithinSearchArea(stationInfo: StationInformation): Boolean = {
    val distanceToRef = computeDistanceToRef(refLat.toDouble, refLon.toDouble)(
      stationInfo.lat.toDouble,
      stationInfo.lon.toDouble
    )
    distanceToRef <= 1
  }

  /**
	 * Processing: deserialize the response to list and apply search area filter
	 * over it
	 **/
  def runRequest(req: HttpRequest)(
      implicit system: ActorSystem,
      executionContext: ExecutionContext
  ): Future[List[StationInformation]] = {
    Http()
      .singleRequest(req)
      .flatMap { res =>
        Unmarshal(res).to[ApiResponseWithAllStationInformationData]
      }
      .map(
        _.data.stations.toList
          .filter(isWithinSearchArea)
      )
  }

  /** Apply processing to one request */
  def collectRelevantStations(
      implicit system: ActorSystem,
      executionContext: ExecutionContext
  ): Future[List[StationInformation]] = runRequest(request)

}
