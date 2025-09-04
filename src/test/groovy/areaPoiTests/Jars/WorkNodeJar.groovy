package areaPoiTests.Jars

import dynamicFarm.run.WorkNode

class WorkNodeJar {
  static void main(String[] args) {
    String FarmerIPaddress = args[0]
    int workers = 0 // uses as many cores as possible
    boolean verbose = false
    new WorkNode(FarmerIPaddress, workers, verbose).invoke()
  }
}