import akka.actor.ActorSystem
import org.mfl.collector.{InformationCollector, StatusCollector}
import org.mfl.models.{Station, StationStatus}

object MainVelibAvailability extends App {

	implicit val system     = ActorSystem()
	implicit val dispatcher = system.dispatcher

	val infoCollector   = new InformationCollector()
	val statusCollector = new StatusCollector()

	def getConsolidateRelevantStations(sList: List[StationStatus]) = {
		for {
			listRelevantStationInformation <- infoCollector.collectRelevantStations
		} yield {
			listRelevantStationInformation.map {
				sInfo => {
					val sStatusOption = sList
						.filter(_.stationCode == sInfo.stationCode).headOption
					sStatusOption.map(Station(_, sInfo))
				}
			}.filter(_.isDefined)
			     .map(_.get)
		}
	}


	def printConsolidatedStation(station: Station) = {
		println(s"${station.status.numBikesAvailable} bikes available at station" +
		        s" ${station.info.name}")
	}

	//Continuously fetch station status endpoint and consolidate the data
	//With station information
	//Print all the stations that are within the search area, with station name
	//and the number of bikes available
	statusCollector.toSink(
		l => {
			getConsolidateRelevantStations(l)
				.map(_.foreach(printConsolidatedStation))
		})

}
