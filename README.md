# Scala Splio Homework

## Application task

### Objective: what do we want?

We would like to show `real time` Velib `availability` `around` a specific location:

- Splio HQ: 27 Boulevard des Italiens, 75002 Paris
- GPS coordinates: 48°52'15.7"N 2°20'07.4"E [48.870983, 2.335390]

#### Search area

We will assume that `around` means a walkable distance from the location.
Thus, we could arbitrarily define walkable as a 5-min walk circle centered at the location.

**For the sake of simplicity, we will use a 1km-radius circle around the location instead as the search area.**
This may approximate a ~6-min walk circle.

The search area can be expressed as a list of the Velib stations that are located within the area.
We could filter stations programmatically by computing the distance in GPS coordinates from the station to the HQ location.

We use the Haversine formula to compute the distance between to points (ref. `https://nathanrooy.github.io/posts/2016-09-07/haversine-with-python/`).

#### Availability

`availability` could mean several things:

- is there at least one available Velib in the search area?
- how many of them in total in the search area?
- (more precise) how many available at each Velib station located in the search area?

We will try to serve the total number of available bikes (both mechanical and electrical) for each station of the search area.
We will display only stations with `number of available bikes > 0`.


#### Real time

As described  here `https://www.velib-metropole.fr/donnees-open-data-gbfs-du-service-velib-metropole`, the data is updated each minute.
**So we will be able to serve updated Velib availibility data each minute at best.**

### Data source: 2 endpoints available

#### Status

The `status` endpoint provides, along a `stationCode`, the number of bikes available at each station.

```
curl https://velib-metropole-opendata.smoove.pro/opendata/Velib_Metropole/station_status.json
```
Example of response stored at `./data/sample-api-status-response.json`

#### Information

The information endpoint provides, along the `stationCode`, the name, and the position of the station in space.

```
curl https://velib-metropole-opendata.smoove.pro/opendata/Velib_Metropole/station_information.json
```
Example of response stored at `./data/sample-api-information-response.json`


### App flow

- fetch the Status endpoint each minute, for each response
    - unmarshall JSON as a list of StationStatus
    - filter station objects (StationStatus) based on availability criteria (`numBikesAvailable > 0`)
    - consolidation of station objects (StationStatus) with StationInformation relevant to search area. This requires a call StationInformation endpoint. It filters and consolidate data at the same time.
    - print the list of stations with both their name and the number of available bikes.

We use part of the Akka framework:

- Akka Http with support for JSON (+ Spray JSON) to handle API requests/responses
- Akka streams to send API requests and process responses as a stream

### Comments

- results are only available in console
- with Akka-Http, we may setup a web server, routing a websocket to push Velib availability updates in browser
- JSON deserialization protocol plays an important part in the task
- deserialization of responses may fail, it should be at least logged, and handled
- this application is missing tests (unit tests [ensure deserialization consistency, check processing logic], integration tests [check endpoints response schemas is aligned with internal models])
- health check for endpoints might be added (~integration test before running the app)
- if we suppose that StationInformation is mostly stable (does not get updated on a daily basis), we could cache it to avoid one unecessary API call each minute.

### Package and run the App (org.mfl.MainVelibAvailability)

You can create a packaged version of the application by using

```
cd path/to/project/root
sbt universal:packageBin
```
That will generate a ZIP file in `target/universal` directory. This archive contains 2 directories :

* bin: script to run the application
* lib: JAR dependencies

In terminal:

```
cd path/to/project/root
chmod +x target/universal/splio-hw-0.1/bin/main-velib-availability
#And finally run :
target/universal/splio-hw-0.1/bin/main-velib-availability
```

## Data task

Dataset `velib_dataset_c271c5d8-6b77-4557-845c-3b449863bbb0.txt`.

We try to compute:

- the average number of available Velib’ per station and per day’s hour to know what
is the best time to take a bike and in which station.
- the average number of available docks per station and per day’s hour to know what
is the best time to put a bike and in which station.
- the Velib’ stations top 3 with the more available bikes per day


The dataset contains rows of JSON.
Looking at one row (`./data/row.json`), it seems we have the state of 10 Velib stations at a given point in time (so at each line).

### Package and run the code (org.mfl.MainData)

```
cd path/to/project/root
sbt universal:packageBin
chmod +x target/universal/splio-hw-0.1/bin/main-data
#And finally run :
target/universal/splio-hw-0.1/bin/main-data
```

### Results

```
Total records: 7175
1 dates in this dataset as expected (1 day)
12 hours in this dataset as expected (12 hours)
21 different stations are available


Task #1
Bikes availability per station, per hour of the day
Filles Saint-Thomas - Place de la Bourse has the best Velib availability (mean=43) in average at 9h


Task #2
Dock availability per station, per hour of the day
Saint-Marc - Feydeau has the best dock availability (mean  = 37) docks available in average at 5h


Task #3
Stations TOP-3
Filles Saint-Thomas - Place de la Bourse is in top-3, with daily Velib availability of 27 bikes on average
Choiseul - Quatre Septembre is in top-3, with daily Velib availability of 25 bikes on average
Uzès - Montmartre is in top-3, with daily Velib availability of 20 bikes on average
```

### Comments

- JSON deserialization protocol also plays its part in the task.

- I am not familiar with exploratory data analysis with Scala. I am used to using python/pandas framework along Jupyter notebooks to perform such work. Thus, it took me quite some time to leverage on the built-in scala libraries to perform aggregations/groupby/means/max which are convenient. Yet, chained `groupby` are not easy to handle. Ideally, we should use some kind of Spark-like Dataframe library to easily perform such computations.



