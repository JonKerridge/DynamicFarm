package areaPoiTests.locality

import dynamicFarm.records.DataInterface
import dynamicFarm.records.WorkDataInterface

class AreaData implements  DataInterface<AreaData> {

  Location areaLocation
  List <Location> nearPoIs
  List <Location> adjacentPoI
  List <Location> distant
  Map distanceToPoI

  AreaData(List p){
    areaLocation = null
  } // areaData

  AreaData(Location a){
    this.areaLocation = a
  } // areaData

  @Override
  AreaData create(Object loc) {
//    println "Creating an AreaData object"
//    Location l = sourceData.getSourceData() as Location
    if (loc  == null)
      return null
    else
      return new AreaData(loc as Location )
  } // create

  void distance(WorkDataInterface workData, List p){
    nearPoIs = []
    double testDistance = p[0] as Double
    int dataMax = (workData as WorkDataInterface).getWorkDataSize()
    for ( i in 0 ..< dataMax){
//      Location poi = (workData as WorkDataInterface).getNextWorkData(i) as Location
      List poiData = workData.getFilteredWorkData(i, null)
      Location poi = poiData[1]
      double distance = areaLocation.euclideanDistance(poi)
      if (distance <= testDistance) nearPoIs << poi
    }
  } // distance

  void adjacent(WorkDataInterface workData, List p){
    double testDistance = p[0] as Double
    adjacentPoI = []
    int dataMax = (workData as WorkDataInterface).getWorkDataSize()
    for ( i in 0 ..< dataMax){
      List poiData = workData.getFilteredWorkData(i, null)
      Location poi = poiData[1]
      double distance = areaLocation.euclideanDistance(poi)
      if (distance <= testDistance) adjacentPoI << poi
    }
  } // adjacent

  void remote(WorkDataInterface workData, List p){
    double testDistance = p[0] as Double
    distant = []
    int dataMax = (workData as WorkDataInterface).getWorkDataSize()
    for ( i in 0 ..< dataMax){
      List poiData = workData.getFilteredWorkData(i, null)
      Location poi = poiData[1]
      double distance = areaLocation.euclideanDistance(poi)
      if (distance <= testDistance) distant << poi
    }
  } // remote

  void poiDistance (WorkDataInterface workData, List p){
    distanceToPoI = [:]
    int dataMax = (workData as WorkDataInterface).getWorkDataSize()
    for ( i in 0 ..< dataMax){
      Location poi = (workData as WorkDataInterface).getNextWorkData(i) as Location
      double distance = areaLocation.euclideanDistance(poi)
      distanceToPoI.put( (poi.id) : distance)
    }
  } //poiDistance

  String toString(){
    String s = "${areaLocation} : "
    nearPoIs.each {loc ->
      s = s + "${loc}, "
    }
    s = s + " = "
    adjacentPoI.each {Location loc ->
      s = s + "${loc}, "
    }
    s = s + " > "
    distant.each {Location loc ->
      s = s + "${loc}, "
    }
    return s
  }
}
