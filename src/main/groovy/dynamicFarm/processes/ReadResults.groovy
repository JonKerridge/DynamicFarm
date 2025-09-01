package dynamicFarm.processes


import dynamicFarm.records.Terminator
import groovy_jcsp.ALT
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput
import jcsp.net2.NetAltingChannelInput

class ReadResults implements CSProcess{
  NetAltingChannelInput fromWB
  ChannelOutput toC
  boolean running
  int terminatedNodesCount  // shared with FarmManager
  Map nodeAddressMap

  /**
   * This defines the actions of the process.*/
  @Override
  void run() {
    running = true
      while (running){
          def inputRecord = fromWB.read()
          if (!(inputRecord instanceof Terminator)){
//            println "ReadResults: is sending $inputRecord to Collect"
            toC.write(inputRecord)
          }
          else {
            terminatedNodesCount = terminatedNodesCount + 1
//            println "RR: terminated = $terminatedNodesCount; map contains ${nodeAddressMap.size()}"
            if (nodeAddressMap.size() == terminatedNodesCount) {
              // all the nodes have terminated
              running = false
            }
          }
    } // while running
    toC.write(new Terminator(nodeIP: "ReadResults"))
//    println "ReadResults: sent termination to collect and terminated itself"
  }
}
