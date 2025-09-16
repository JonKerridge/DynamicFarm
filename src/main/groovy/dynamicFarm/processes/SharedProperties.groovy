package dynamicFarm.processes

import com.sun.jna.platform.win32.NTSecApi
import groovy_jcsp.ALT
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput
import jcsp.net2.NetChannelOutput

class SharedProperties implements CSProcess{
  // Shared properties between various of the processes,
  // this process is a pure server and guarantees to respond to any
  // input request immediately.  It never outputs without a request first.
  boolean terminating = false
  // used to avoid problems initiating a new node while termination in progress
  // it is shared between Emit (writes) and FarmManager(reads)
  Map<String, NetChannelOutput> NodeIPAddressMap = [:]
  // contains the value of a Net Channel Output that is used to write emitted objects from
  //SendWork to a ReadBuffer in a Node.  The NodeIP acts as the key.
  // The map is updated in FarmManager and read from SendWork
  int nTerminatedNodes = 0
  // used to count the number of nodes that have terminated
  // incremented in ReadResults and accessed in RequestManager
  int nActiveNodes = 0
  // used to count the number of active nodes
  // updated in FarmManager and accessed in ReadResults

 // Access channels from other Farmer processes
  ChannelInput fromE, fromFM, fromSW, fromRR, fromC
  ChannelOutput toFM, toSW, toRR

  @Override
  void run() {
//    println "SP starting"
    boolean running = true
    ALT spAlt = new ALT([fromE, fromFM, fromSW, fromRR, fromC])
    while (running) {
      switch (spAlt.select()) {
        case 0: //fromE
          terminating = fromE.read() as boolean
          break
        case 1: //fromFM
          List fmRequest = fromFM.read() as List
          if (fmRequest[0] == "T")
            toFM.write(terminating)
          else {
            // update of node address map
            NodeIPAddressMap.put(fmRequest[1] as String, fmRequest[2] as NetChannelOutput)
            nActiveNodes = nActiveNodes + 1
          }
          break
        case 2: //fromSW
          String nodeIP = fromSW.read() as String
          toSW.write(NodeIPAddressMap.get(nodeIP))
          break
        case 3: //fromRR
          fromRR.read() // just a signal
          nTerminatedNodes = nTerminatedNodes + 1
          toRR.write(nTerminatedNodes == nActiveNodes)
          break
        case 4: // fromC
          fromC.read()  // a signal
          running = false
      } // switch
    } // running
//    println "SP terminating"
  } // run
}
