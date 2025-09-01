package dynamicFarm.records

class ExtractParameters {

/**
 * in the specification of parameter strings the first string is a comma separated list of types
 * this is passed to extractParams as tList
 *
 * there are a number of parameter value strings
 * also a comma separated string whose type matches the corresponding entry in tList
 *
 * pList holds one of the strings
 */
  static def extractParams = { List tList, List pList ->
//      println "params to be processed = $tList, $pList"
    List params = []
    if (pList == null) return params
    assert tList.size() == pList.size(): "Extract Parameters: type and value list not the same size"
    int pointer
    pointer = 0
    int tSize = tList.size()   // each param spec comprises type-specification value
    while( pointer < tSize ){
      String pType = tList[pointer]
      String pString = pList[pointer]
      switch (pType){
        case 'int':
          params << Integer.parseInt(pString)
          break
        case 'float':
          params << Float.parseFloat(pString)
          break
        case 'String':
          params << pString
          break
        case 'double':
          params << Double.parseDouble(pString)
          break
        case 'long':
          params << Long.parseLong(pString)
          break
        case 'boolean':
          params << Boolean.parseBoolean(pString)
          break
        default:
          println "Processing parameter string unexpectedly found type = $pType, value = $pString]"
          break
      } // end switch
      pointer++
    } // while
//      println "returned params = $params"
    return params
  } // extract params

}
