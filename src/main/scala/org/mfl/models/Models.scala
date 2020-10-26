package org.mfl.models

case class StationInformation(
	                             stationId: Int,
	                             name: String,
	                             lat: Float,
	                             lon: Float,
	                             capacity: Int,
	                             stationCode: String,
	                             rental_methods: Option[List[String]]
                             )

case class StationInformationData(stations: Seq[StationInformation])

case class ApiResponseWithAllStationInformationData(
	                                                   lastUpdatedOther: Int,
	                                                   ttl: Int,
	                                                   data: StationInformationData
                                                   )

case class StationStatus(
	                        stationCode   : String,
	                        stationId     : Int,
	                        numDocksAvailable: Int,
	                        numBikesAvailable: Int,
	                        isInstalled: Int,
	                        isRenting: Int,
	                        isReturning: Int,
	                        lastReported: Int,
                        )

case class StationStatusData(stations: Seq[StationStatus])

case class ApiResponseWithAllStationStatusData(
	                                              lastUpdatedOther: Int,
	                                              ttl: Int,
	                                              data: StationStatusData
                                              )

case class Station(status: StationStatus, info: StationInformation)
