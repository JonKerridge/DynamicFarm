package dynamicFarm.records

/**
 *
 */
class ParseRecord implements Serializable {
  String typeName                 // host, emit, source, work, collect
  String hostAddress
  String version
  String sourceDataFileName //the name of the source data file  or folder containing files
  String workMethodName // name of method to be executed in work nodeLoader
  String workDataFileName  //the name of a work data file or folder containing files
  List <String> workParameters  // used to construct work class
  List <String> dataParameters // used to construct data class
  List<String> resultParameters  // used to construct result class
  List <String> collectParameters   // list of collectParameters used by collect method
  List <String> finaliseParameters  // list of finaliseParameters used in finalise method

  // all parameter lists are specified as [comma separated type list]![comma separated value list]
  // both lists are the same length


  ParseRecord(){
    workParameters = []
    dataParameters = []
    resultParameters = []
    collectParameters = []
    finaliseParameters = []

  } // constructor

  @Override
  String toString() {
    String s = "type= $typeName, host= $hostAddress, version = $version,   " +
        "\n\tdata class Params= $dataParameters" +
        "\n\tsource data files = $sourceDataFileName" +
        "\n\twork method= $workMethodName, work params=$workParameters, data file = $workDataFileName, " +
        "\n\tresult class params= $resultParameters," +
        "\n\tcollect method params= $collectParameters," +
        "\n\tfinalise method params= $finaliseParameters,"
    return s
  }
}
