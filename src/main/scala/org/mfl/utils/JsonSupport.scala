package org.mfl.utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.mfl.models.{ApiResponseWithAllStationInformationData,
	ApiResponseWithAllStationStatusData, StationInformation,
	StationInformationData, StationStatus, StationStatusData}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
	implicit val stationInformationFormat: RootJsonFormat[StationInformation] = jsonFormat(
		StationInformation,
		"station_id",
		"name",
		"lat",
		"lon",
		"capacity",
		"stationCode",
		"rental_methods"
		)

	implicit val stationsDataFormat                     = jsonFormat1(
		StationInformationData
		)
	implicit val apiResponseWithAllStationInformationDataFormat   = jsonFormat3(
		ApiResponseWithAllStationInformationData
		)

	implicit val stationStatusFormat: RootJsonFormat[StationStatus]      = jsonFormat(
			StationStatus,
			"stationCode",
			"station_id",
			"numDocksAvailable",
			"numBikesAvailable",
			"is_installed",
			"is_returning",
			"is_renting",
			"last_reported"
			)
	implicit val stationStatusDataFormat                     = jsonFormat1(
		StationStatusData
		)
	implicit val apiResponseWithAllStationStatusDataFormat   = jsonFormat3(
		ApiResponseWithAllStationStatusData
		)
}

object JsonSupport extends JsonSupport

object MainJsonSupport extends App {
	import JsonSupport._
	import spray.json._

	val jsonStationInformationString =
		"""{"lastUpdatedOther":1603742052,"ttl":3600,"data":{"stations":[{"station_id":213688169,"name":"Benjamin Godard - Victor Hugo","lat":48.865983,"lon":2.275725,"capacity":35,"stationCode":"16107"},{"station_id":99950133,"name":"André Mazet - Saint-André des Arts","lat":48.85375581057431,"lon":2.3390958085656166,"capacity":55,"stationCode":"6015","rental_methods":["CREDITCARD"]}]}}"""
	val jsonStationInformation       = jsonStationInformationString.parseJson.convertTo[ApiResponseWithAllStationInformationData]
	println(jsonStationInformation)

	val jsonStationStatusString =
		"""{"lastUpdatedOther":1603742052,"ttl":3600,"data":{"stations":[{"stationCode":"16107","station_id":213688169,"num_bikes_available":1,"numBikesAvailable":1,"num_bikes_available_types":[{"mechanical":0},{"ebike":1}],"num_docks_available":33,"numDocksAvailable":33,"is_installed":1,"is_returning":1,"is_renting":1,"last_reported":1603741467},{"stationCode":"6015","station_id":99950133,"num_bikes_available":22,"numBikesAvailable":22,"num_bikes_available_types":[{"mechanical":17},{"ebike":5}],"num_docks_available":32,"numDocksAvailable":32,"is_installed":1,"is_returning":1,"is_renting":1,"last_reported":1603742043}]}}"""
	val jsonStationStatus            = jsonStationStatusString.parseJson.convertTo[ApiResponseWithAllStationStatusData]
	println(jsonStationStatus)
}