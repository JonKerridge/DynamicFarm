package dynamicFarm.processes


import dynamicFarm.records.DataInterface
import dynamicFarm.records.Terminator
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput
import jcsp.net2.NetChannelOutput

class SendWork implements CSProcess{
  ChannelInput fromE, fromRM, fromSP
  ChannelOutput toRM, toSP
  boolean running
  List <NetChannelOutput> chanList =[]

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
            toRM.write("STOP")
//            println "SendWork has received termination from Emit"
          }
          else {
//            println "SW: sending request to RM"
            toRM.write("GET")  // used as a signal
//            println "SW: awaiting response from RM"
            String nodeIP = fromRM.read()
//            println "SW: receiving response from RM - $nodeIP"
            toSP.write(nodeIP)
            NetChannelOutput outChan = fromSP.read() as NetChannelOutput
            outChan.write(emitData as DataInterface)
            if (!chanList.contains(outChan)) chanList << outChan
          }
    } // running
    // now terminate the nodes
    chanList.each { chan ->
      chan.write(new Terminator(nodeIP: "SendWork"))
    }
//    println "SendWork has terminated"
  } // run()
}
