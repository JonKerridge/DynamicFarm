package dynamicFarm.processes

import dynamicFarm.records.DataInterface
import dynamicFarm.records.Terminator
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.net2.NetChannelOutput

class WriteBuffer implements CSProcess {
  NetChannelOutput outputToResults
  ChannelInput fromInternals
  int nInternals
  String nodeIP

  @Override
  void run() {
    int terminated
    boolean running
    (terminated, running) = [0, true]
    while (running) {
      def object = fromInternals.read()
//      println "WB [$nodeIP]: has read $object"
      if (object instanceof Terminator) {
        terminated = terminated + 1
        if (terminated == nInternals) running = false
//            println "WB [$nodeIP]: terminating $terminated and $running"
      }
      else {
        outputToResults.write(object as DataInterface)
//        println "WB [$nodeIP]: has written $object to ReadResults"
      }

    } // running
    outputToResults.write(new Terminator(nodeIP: nodeIP))
//    println "WriteBuffer in $nodeIP has terminated"
  } // run
}