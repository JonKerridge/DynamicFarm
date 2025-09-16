package dynamicFarm.processes

import dynamicFarm.records.ResultInterface
import dynamicFarm.records.DataInterface
import dynamicFarm.records.Terminator
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import dynamicFarm.records.ExtractParameters
import jcsp.lang.ChannelOutput

class Collect implements CSProcess{
  ChannelInput fromRR  // connects to ReadResults
  ChannelOutput toFM, toSP   // to send terminator from Collect to FM and SP
  // class associated with the collector that defines the collate and finalise methods
  Class <?> resultClass
  List <String> classParameters
  List <String> collectParameters
  List <String> finaliseParameters

  /**
   * This defines the actions of the process.*/
  @Override
  void run() {
//    println "Collect: running"
    Class[] cArg = new Class[1]
    cArg[0] = List.class
    List collectParameterValues, finaliseParameterValues, classParameterValues
    classParameterValues = []
    collectParameterValues = []
    finaliseParameterValues = []
    // initialise the Collect class
    if (classParameters != null) {
      classParameterValues = ExtractParameters.extractParams(classParameters[0] as List, classParameters[1] as List)
    }
    def collectInstance = resultClass.getDeclaredConstructor(cArg).newInstance(classParameterValues)
    // get parameter values for Collate and finalise
    if (collectParameters != null )
      collectParameterValues = ExtractParameters.extractParams(collectParameters[0] as List, collectParameters[1] as List)
    if (finaliseParameters != null )
      finaliseParameterValues = ExtractParameters.extractParams(finaliseParameters[0] as List, finaliseParameters[1] as List)
    boolean running
    running = true
    while (running) {
      def object = fromRR.read()
      if (object instanceof Terminator) {
        running = false
//        println "Collect: is terminating "
      }
      else { // process incoming data object
//        println "Collect: has read $object "
        (collectInstance as ResultInterface).collate((object as DataInterface), collectParameterValues)
      }
    } // running
    //call the finalise method if it exists and close the toSW stream
    (collectInstance as ResultInterface).finalise(finaliseParameterValues)
    // send terminator to FarmManager and SP
    toSP.write(1)
    toFM.write((new Terminator(nodeIP: "STOP")))
//    println "Collect has terminated"
  }
}
