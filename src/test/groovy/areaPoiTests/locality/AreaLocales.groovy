package areaPoiTests.locality

import dynamicFarm.records.SourceDataInterface

class AreaLocales implements SourceDataInterface<Location> {

  List <Location> areas
//  List areas
  int nAreas, current

  AreaLocales (String fileName){
//    println "Importing source data from $fileName"
    areas = []
    File objFile = new File(fileName)
    objFile.withObjectInputStream { inStream ->
      inStream.eachObject {
        areas << it
      }
    }
    nAreas = areas.size()
//    println "Created areas in Emitter: $nAreas"
//    areas.each{println "$it"}
    current = 0
  }

  @Override
  Location getSourceData() {
    if (current < nAreas){
      Location r = areas[current]
      current = current + 1
      return r
    }
    else
      return null
  }

}
