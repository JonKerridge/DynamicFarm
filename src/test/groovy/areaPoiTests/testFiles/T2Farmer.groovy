package areaPoiTests.testFiles

import areaPoiTests.locality.AreaData
import areaPoiTests.locality.AreaLocales
import areaPoiTests.locality.AreaPoICollect
import areaPoiTests.locality.PoILocales
import dynamicFarm.run.Farmer


class T2Farmer {
  static void main(String[] args) {
    String structure =  "D:/IJGradle/DynamicFarm/src/test/groovy/areaPoiTests/testFiles/Test2"
    Class dataClass = AreaData
    Class sourceDataClass = AreaLocales
    Class workDataClass = PoILocales
    Class resultClass = AreaPoICollect
    new Farmer( structure, dataClass, sourceDataClass, workDataClass,
        resultClass,"Local", false ).invoke()
  }
}
