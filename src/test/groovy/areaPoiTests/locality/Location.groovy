package areaPoiTests.locality

class Location implements Serializable{
  String type
  int id, x, y

  Location(String type,
           int id,
           int x,
           int y ) {
    this.type = type
    this.id = id
    this.x = x
    this.y = y
  }
/**
 * Calculates the Euclidean Distance between this point and another point
 *
 * @param poi the location of a point of interest as a Location
 * @return the distance as the crow flies between this point and the locale defined by poi
 */
  double euclideanDistance (Location poi){
    int x = Math.abs(this.x - poi.x)
    int y = Math.abs(this.y - poi.y)
    return Math.sqrt( (x*x) as Double + (y*y) as Double)
  }

  /**
   * Determines if two points are colocated at same co-ordinates
   * @param poi the location of a point of interest as a Location
   * @return true if the the two points have the same co-ordinates, false otherwise
   */
  boolean equal(Location poi){
    return ( (this.x == poi.x) && (this.y == poi.y))
  }

  /**
   * used to compare in sort and unique operations using the spaceship operator
   * @param poi the location of a point of interest as a Location
   * @return an indication of the comparison: = - 0, < - -1, > - 1
   */
  int compareTo(Location poi){
    this.id <=> poi.id
  }

  String toString(){
    return "$type-$id: [$x, $y]"
  }

}
