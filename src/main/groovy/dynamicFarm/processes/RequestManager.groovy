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
  ChannelInput fromSW
  ChannelOutput toSW

  // local queue
  List <String> requests = []


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
          RequestWork inputRequest = fromRB.read() as RequestWork
          requests << inputRequest.nodeIP
          entries = entries + 1
//          println "RM: node request received $requests, entries $entries"
          break
        case 1: // fromSW get a nodeIP entry
//          println "RM: processing a request from SW"
          String signal = fromSW.read() // just a signal so do not need value
          if (signal == "GET") {
//          println "RM: request from SW"
            String nodeIP = requests.pop()
            toSW.write(nodeIP)
            entries = entries - 1
//          println "RM: sent $nodeIP to SW now got $entries entries"
          } else
            running = false
          break
      } // switch
    } //while
//    println "Request Manager terminated"
  } //run
}
