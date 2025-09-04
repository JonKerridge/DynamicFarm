package areaPoiTests.testFiles

import areaPoiTests.locality.AreaData
import areaPoiTests.locality.AreaLocales
import areaPoiTests.locality.AreaPoICollect
import areaPoiTests.locality.PoILocales
import dynamicFarm.run.Farmer


class T1Farmer {
  static void main(String[] args) {
    String structure =  "./src/test/groovy/areaPoiTests/testFiles/Test1"
    Class dataClass = AreaData
    Class sourceDataClass = AreaLocales
    Class workDataClass = PoILocales
    Class resultClass = AreaPoICollect
    new Farmer( structure, dataClass, sourceDataClass, workDataClass,
        resultClass,"Local", false ).invoke()
  }
}
