package org.mfl.collector

import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import org.mfl.models.{ApiResponseWithAllStationStatusData, StationStatus}
import org.mfl.utils.JsonSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
 * Connector to the Velib' `Station Status` endpoint.
 *
 * Fetch, deserialize/load to case class and filter Stations based on
 * bike availability predicate.
 */
class StatusCollector extends JsonSupport {

	val uri = "https://velib-metropole-opendata.smoove" +
	          ".pro/opendata/Velib_Metropole/station_status.json"

	// Tick the endpoint every 60 seconds
	val requests: Source[HttpRequest, Cancellable] = Source.tick(
		0 seconds,
		60 seconds,
		HttpRequest(uri = uri)
		)

	/**
	 * Bike availability predicate: True if at least 1 bike is available at the
	 * station
	 **/
	def hasAvailableBikes(stationStatus: StationStatus): Boolean = {
		//True if some bikes are available on the station
		stationStatus.numBikesAvailable > 0
	}

	/**
	 * Processing: deserialize the response to list and apply filter
	 **/
	def runRequest(req: HttpRequest)
	              (implicit system                  : ActorSystem,
	               executionContext                 : ExecutionContext)
	: Future[List[StationStatus]]
	= {
		Http()
			.singleRequest(req)
			.flatMap {
				res => Unmarshal(res).to[ApiResponseWithAllStationStatusData]
			}.map(_.data
			       .stations
			       .toList
			       .filter(hasAvailableBikes)
			      )
	}

	/**
	 * Continuously process requests and apply sink method on results
	 * (`println` for instance)
	 */
	def toSink(pf  : List[StationStatus] => Unit)(implicit system: ActorSystem,
	                                              executionContext: ExecutionContext)
	: Unit = {
		requests
			.mapAsync(1)(req => runRequest(req))
			.runWith(Sink.foreach(pf))
	}


}
