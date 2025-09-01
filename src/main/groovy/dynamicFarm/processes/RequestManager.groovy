package dynamicFarm.processes

import dynamicFarm.records.RequestWork
import dynamicFarm.records.Terminator
import groovy_jcsp.ALT
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput
import jcsp.net2.NetAltingChannelInput

class RequestManager implements CSProcess{
  NetAltingChannelInput fromRB
  ChannelInput fromSW, fromFM
  ChannelOutput toSW

  // local queue properties
  List <String> requests = []

  // shared property
  Map nodeAddressMap

  /**
   * This defines the actions of the process.*/
  @Override
  void run() {
    int entries, terminated
    (entries, terminated) = [0, 0]
//    println "RM: starting"
    ALT RMAlt = new ALT([fromRB, fromSW])
    boolean [] preCon
    boolean running
    running = true
    while (running){
      preCon = [true, entries>0]
      switch (RMAlt.select(preCon)){
        case 0: // fromRB add a request nodeIP or Terminator
          def inputRequest = fromRB.read()
          if (inputRequest instanceof Terminator){
            terminated = terminated + 1
//            println "RM: got terminator terminated = $terminated, nodes = ${nodeAddressMap.size()}"
            if (terminated == nodeAddressMap.size())
              running = false
          }
          else {
            requests << (inputRequest as RequestWork).nodeIP
            entries = entries + 1
//          println "RM: node request received $requests, entries $entries"
          }
          break
        case 1: // fromSW get a nodeIP entry
//          println "RM: processing a request from SW"
          def signal = fromSW.read() // just a signal so do not need value
//          println "RM: request from SW"
          String nodeIP = requests.pop()
          toSW.write(nodeIP)
          entries = entries - 1
//          println "RM: sent $nodeIP to SW now got $entries entries"
          break
      } // switch
    } //while
//    println "Request Manager terminated"
  } //run
}
