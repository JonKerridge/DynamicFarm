package areaPoiTests.fileOperations

import areaPoiTests.locality.Location


class CreateAreaFile {
  static void main(String[] args) {
    int range = 175
    int entries = 1000
    String outFileString = "./data/areas1000.loc"
    File objFile = new File(outFileString)
    if ( objFile.exists()) objFile.delete()
    ObjectOutputStream outStream = objFile.newObjectOutputStream()
    String type = "area"
    int x, y
    Random ran = new Random()
    for ( i in 1 .. entries){
      x = ran.nextInt(range) + 1
      y = ran.nextInt(range) + 1
      Location loc = new Location(type, i, x, y)
      println "$loc"
      outStream.writeObject(loc)
    }
    outStream.flush()
    outStream.close()
  }
}
