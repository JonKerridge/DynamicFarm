package areaPoiTests.Jars

import areaPoiTests.locality.AreaData
import areaPoiTests.locality.AreaLocales
import areaPoiTests.locality.AreaPoICollect
import areaPoiTests.locality.PoILocales
import dynamicFarm.run.Farmer

class FarmerJar {
  static void main(String[] args) {
    String structure = args[0]  // name of df file without suffix
    Class dataClass = AreaData
    Class sourceData = AreaLocales
    Class workData = PoILocales
    Class resultClass = AreaPoICollect
    boolean verbose = false
    new Farmer(structure, dataClass, sourceData, workData, resultClass, "Net", verbose).invoke()
  }
}

