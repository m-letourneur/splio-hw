# Scala Splio Homework

## Application

### Objective: what do we want?

We would like to show `real time` Velib `availability` `around` a specific location:
> Splio HQ: 27 Boulevard des Italiens, 75002 Paris
> GPS coordinates: 48°52'15.7"N 2°20'07.4"E [48.870983, 2.335390]


#### Search area

We will assume that `around` means a walkable distance from the location.
Thus, we could arbitrarily define walkable as a 5-min walk circle centered at the location.

**For the sake of simplicity, we will use a 1km-radius circle around the location instead as the search area.
This may approximate a ~6-min walk circle.**

The search area can be expressed as a list of the Velib stations that are located within the area.
This list of stations can be encoded as the list of `stationcode`, as described in the data model [`https://opendata.paris.fr/explore/dataset/velib-disponibilite-en-temps-reel/information/?disjunctive.name&disjunctive.is_installed&disjunctive.is_renting&disjunctive.is_returning&disjunctive.nom_arrondissement_communes`].
We could filter stations programmatically by computing the distance in GPS coordinates from the station to the location, using the GPS coordinates with the field `coordonnees_geo`.

We use the following formula to compute the distance between to points :

```
dist = arccos(sin(lat1) · sin(lat2) + cos(lat1) · cos(lat2) · cos(lon1 - lon2)) · R

lat1, lon1, lat2, lon2 in radians
Earth with radius R=6371 km

```


#### Availability

`availability` could mean several things:
- is there at least one available Velib in the search area?
- how many of them in total in the search area?
- (more precise) how many available at each Velib station located in the search area?

We will first try to serve the total number of available bikes (both mechanical and electrical) for each station of the search area.
We will display only stations with `number of available bikes > 0`.


#### Real time

We are able to fetch data from `https://opendata.paris.fr/explore/dataset/velib-disponibilite-en-temps-reel/` to get Velib availability information across all stations.
This data is updated each minute.
**So we will be able to serve updated Velib availibility data each minute at best.**


### Data source: 2 endpoints

#### Status

The status endpoint provides, along a `stationCode`, the number of bikes available.

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


### Suggested Flow

- fetch Status endpoint each minute, for each response
    - unmarshall JSON StationStatus
    - filter station objects (StationStatus) based on availability criteria (`numbikesavailable > 0`)
    - consolidation station objects objects (StationStatus) with StationInformation relevant to search area
    This requires a call StationInformation endpoint. It filters and consolidate data at the same time.
    - print the list of stations with both their name and the number of available bikes.



