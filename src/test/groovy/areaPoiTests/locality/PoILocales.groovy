package areaPoiTests.locality

import dynamicFarm.records.WorkDataInterface

class PoILocales implements WorkDataInterface<Location> {

  List <Location> pois
  int nPoi

  PoILocales(String fileName){
    pois = []
    File objFile = new File(fileName)
    objFile.withObjectInputStream { inStream ->
      inStream.eachObject {
        pois << it
      }
    }
    nPoi = pois.size()
  }

//  @Override
//  Location getNextWorkData(int index) {
//    // assume index is valid!!
//    return pois[index]
//  }

/**
 * This method gets the next item of work data having used the values
 * in filterValues to ignore any work data items that do not satisfy the
 * filter.
 * The filterValue(s) will be obtained from the object implementing the
 * DataInterface using the getFilterValues method.
 * If the work data does not need filtering then filterValues should be null.
 *
 * @param index the subscript in the Work Data, from where the search will start
 * @param filterValues the value(s) used to undertake the filtering operation
 * @return a List comprising [index, T], where index is the next subscript from
 * which to start the next search and T is an object of type T that is being returned
 * for processing.  T will be null when no more data is available.
 *
 * Note that this formulation is required so that the WorkData object does not have
 * to retain state for the many processes that are sharing access.
 */

  @Override
  List getFilteredWorkData(int index, List filterValues) {
    return [index, pois[index]]
  }

  @Override
  int getWorkDataSize() {
    return nPoi
  }
}
