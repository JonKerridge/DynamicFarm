package dynamicFarm.processes


import dynamicFarm.records.DataInterface
import dynamicFarm.records.Terminator
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput
import jcsp.net2.NetChannelOutput

class SendWork implements CSProcess{
  ChannelInput fromFM, fromE, fromRM
  ChannelOutput toRM
  boolean running
  DataInterface buffer
  // shared with FarmManager
  Map<String, NetChannelOutput> nodeAddressMap

  /**
   * This defines the actions of the process.*/
  @Override
  void run() {
//    println "SendWork starting"
    running = true
    while (running) {
          def emitData = fromE.read()
//          println "SendWork receiving $emitData from emit"
          if (emitData instanceof Terminator) {
            running = false
//            println "SendWork has received termination from Emit"
          }
          else {
//            println "SW: sending request to RM"
            toRM.write("GET")  // used as a signal
//            println "SW: awaiting response from RM"
            String nodeIP = fromRM.read()
//            println "SW: receiving response from RM - $nodeIP"
            (nodeAddressMap.get(nodeIP) as NetChannelOutput).write(emitData as DataInterface)
//            println "SendWork has received $emitData from Emit and sent it to RB in $nodeIP"
          }
    } // running
    nodeAddressMap.each{entry->
      String nodeIP = entry.key
//      println "SendWork processing $nodeIP when terminating"
      (nodeAddressMap.get(nodeIP) as NetChannelOutput).write(new Terminator(nodeIP: "emit"))
     }
//    println "SendWork has terminated"
  } // run()
}
