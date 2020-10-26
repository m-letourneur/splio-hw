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

class InformationCollector extends JsonSupport {

	val uri = "https://velib-metropole-opendata.smoove" +
	          ".pro/opendata/Velib_Metropole/station_information.json"

	val request: HttpRequest = HttpRequest(uri = uri)

	val refLat = 48.870983
	val refLon = 2.335390

	//todo
	def computeDistanceToRef(refLat: Double, refLon: Double)(lat: Double,
	                                                       lon: Double) = {
		val refLatRad = refLat*Math.PI/180
		val refLonRad = refLon*Math.PI/180
		val latRad = lat*Math.PI/180
		val lonRad = lon*Math.PI/180

		Math.acos(Math.sin(refLatRad) * Math.sin(latRad) + Math.cos(refLatRad) * Math.cos
		(latRad) * Math.cos(refLonRad - lonRad)) * 6371

	}

	def isWithinSearchArea(stationInfo: StationInformation): Boolean = {
		val distToRef = computeDistanceToRef(refLat.toDouble,
		                                     refLon.toDouble)(stationInfo.lat
		                                                                 .toDouble,
		                                                     stationInfo.lon
		                                                                .toDouble)
		distToRef <= 1
	}

	def runRequest(req: HttpRequest)
	              (implicit system: ActorSystem,
	               executionContext: ExecutionContext)
	: Future[List[StationInformation]]
	= {
		Http()
			.singleRequest(req)
			.flatMap {
				res => Unmarshal(res).to[ApiResponseWithAllStationInformationData]
			}.map(_.data
			       .stations
			       .toList
			       .filter(isWithinSearchArea)
			      )
	}

	def collectRelevantStations(implicit system  : ActorSystem,
	                            executionContext : ExecutionContext)
	: Future[List[StationInformation]]
	= runRequest(request)

}
