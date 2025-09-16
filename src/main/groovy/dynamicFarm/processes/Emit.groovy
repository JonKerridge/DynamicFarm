package dynamicFarm.processes


import dynamicFarm.records.DataInterface
import dynamicFarm.records.SourceDataInterface
import dynamicFarm.records.Terminator
import dynamicFarm.records.ExtractParameters
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput

class Emit implements CSProcess{
  ChannelInput fromFM
  ChannelOutput toSW, toSP
  Class classDef, sourceDef
  List emitParams
  String sourceDataFileName

  /**
   * This defines the actions of the process.*/
  @Override
  void run() {
    SourceDataInterface sourceData
    sourceData = null
    if ((sourceDataFileName != null) && (sourceDef != null) ){
      // the declared constructor has a single String containing filename as parameter
      Class[] cArg = new Class[1]
      cArg[0] = String.class
      sourceData = sourceDef.getDeclaredConstructor(cArg).newInstance(sourceDataFileName) as SourceDataInterface
    }
    else {
      if ( !(sourceDataFileName == null) && (sourceDef == null)){
        // there is a problem because either both must be null or both not null (see above)
        println "Inconsistency in source data specification" +
            "\nsource data file name is $sourceDataFileName and source class name id ${sourceDef.getName()}" +
            "\neither both must be null or both must have values"
        System.exit(-3)
      }
    }
    // have to wait for a go message from control, which happens only once a node has started and is running
    String goMessage = fromFM.read() as String
    assert  goMessage == "GO":" Emit expected GO message but got $goMessage"
    List parameterValues
    parameterValues = []
    if (emitParams != null)
      parameterValues = ExtractParameters.extractParams(emitParams[0] as List, emitParams[1] as List)
    println "Emit  parameters $parameterValues, source data file $sourceDataFileName"
    Class[] cArg = new Class[1]
    cArg[0] = List.class
    Object emitClass = classDef.getDeclaredConstructor(cArg).newInstance(parameterValues)
    Object ec
    if (sourceData  == null)
      ec = (emitClass as DataInterface).create(null)
    else
      ec = (emitClass as DataInterface).create((sourceData).getSourceData())
//    println "Emit has initially created $ec"
    while (ec != null) {
      toSW.write(ec)
//      println "Emit has written $ec"
      if (sourceData  == null)
        ec = (emitClass as DataInterface).create(null)
      else
        ec = (emitClass as DataInterface).create((sourceData).getSourceData())
//      println "Emit has created $ec"
    } //while
    // set terminating in SP
    toSP.write(true as boolean)
    toSW.write(new Terminator(
        nodeIP: "EMIT end" )
    )
//    println "Emit has terminated "

  }  // run
}
