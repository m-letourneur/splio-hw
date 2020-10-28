package org.mfl

import java.time.OffsetDateTime

import models.{NormalizedStationRecord, Row}
import spray.json._
import utils.JsonSupport._
import scala.io.Source

/**
 * Main for the Data task
 */
object MainData extends App {

	val absPath        = "./data" +
	                     "/velib_dataset_c271c5d8-6b77-4557-845c" +
	                     "-3b449863bbb0.txt"
	val source: String = Source.fromFile(absPath).getLines.mkString

	//Parse lines of the file and deserialize JSONs
	val rows = Source.fromFile(absPath).getLines.map { line =>
		val json = line.parseJson
		json.convertTo[Row]
	}
	val s   = rows.toList.flatMap(_.records)
	val len = s.length
	println(s"Total records: $len units")

	//Normalize records (and datetime)
	val normalizedS = s.map(rec =>
		                        NormalizedStationRecord(
			                        stationName = rec.fields.stationName,
			                        stationCode = rec.fields.stationCode,
			                        nbEBike = rec.fields.nbEBike,
			                        nbFreeDock = rec.fields.nbFreeDock,
			                        nbBike = rec.fields.nbBike,
			                        timestamp = OffsetDateTime.parse(rec
				                                                         .recordTimestamp),
			                        )
	                        )

	// Describe dataset
	val nbDates = normalizedS.groupBy(rec => (rec.timestamp.getDayOfYear, rec
		.timestamp.getYear)).size
	println(s"$nbDates dates in this dataset as expected (1 day)")

	val nbHours = normalizedS.groupBy(rec => (rec.timestamp.getHour)).size
	println(s"$nbHours hours in this dataset as expected (12 hours)")

	val nbStations = normalizedS.groupBy(rec => (rec.stationCode)).size
	println(s"$nbStations different stations are available")

	//Group by station
	val gbPerStation        = normalizedS.groupBy(_.stationName)
	val gbPerStationPerHour = gbPerStation.mapValues(listRec =>
		                                                 listRec
			                                                 .groupBy(_.timestamp
			                                                           .getHour))

	/**
	 * Task #1
	 * Bikes availability
	 */
	println("\n\nTask #1")
	println("Bikes availability per station, per hour of the day")

	def computeMean(intL: List[Int]) = {
		if (intL.isEmpty) 0 else intL.sum / intL.length
	}

	val gbPerStationPerHourAvailableBikesMean = gbPerStation
		.mapValues(listRec =>
			           listRec
				           .groupBy(_.timestamp.getHour).mapValues(
				           l =>
					           computeMean(l.map(_.nbBike))
				           )
		           )
	// Then we need to find the max of these means to get the hour and the
	// station with highest Velib availability
	val bestHour                              =
		gbPerStationPerHourAvailableBikesMean.mapValues(_.maxBy(_._2)).maxBy(_._2._2)._2._1
	val bestAvailability                      =
		gbPerStationPerHourAvailableBikesMean.mapValues(_.maxBy(_._2)).maxBy(_._2._2)._2._2
	val bestStation                           =
		gbPerStationPerHourAvailableBikesMean.mapValues(_.maxBy(_._2)).maxBy(_._2._2)._1
	println(s"$bestStation has the best Velib availability " +
	        s"(mean=${bestAvailability}) in average at ${bestHour}h")

	/**
	 * Task #2
	 * Dock availability
	 */
	println("\n\nTask #2")
	println("Dock availability per station, per hour of the day")
	val gbPerStationPerHourAvailableDocksMean = gbPerStation
		.mapValues(listRec =>
			           listRec
				           .groupBy(_.timestamp.getHour).mapValues(
				           l =>
					           computeMean(l.map(_.nbFreeDock))
				           )
		           )

	// Then we need to find the max of these means to get the hour and the
	// station with highest dock availability
	val bestDockHour         = gbPerStationPerHourAvailableDocksMean.mapValues(_.maxBy(_._2)).maxBy(_._2._2)._2._1
	val bestDockAvailability = gbPerStationPerHourAvailableDocksMean.mapValues(_.maxBy(_._2)).maxBy(_._2._2)._2._2
	val bestDockStation      = gbPerStationPerHourAvailableDocksMean.mapValues(_.maxBy(_._2)).maxBy(_._2._2)._1

	println(s"$bestDockStation has the best dock availability " +
	        s"(mean  = ${bestDockAvailability}) docks available in average at " +
	        s"${bestDockHour}h")

	/**
	 * Task #3
	 * Best daily stations
	 */
	println("\n\nTask #3")
	println("Stations TOP-3")
	val gbPerStationDailyMean = gbPerStation.mapValues(listRec =>
		                                                   computeMean(listRec.map
		                                                   (_.nbBike))
	                                                   )
	val threeBestStations     = gbPerStationDailyMean.toSeq.sortBy(_._2).reverse
	                                                 .take(3)
	threeBestStations.foreach(rec =>
		                          println(s"${rec._1} is in top-3, with daily " +
		                                  s"Velib availability of ${rec._2} bikes" +
		                                  s" " +
		                                  s"on average")
	                          )
}
