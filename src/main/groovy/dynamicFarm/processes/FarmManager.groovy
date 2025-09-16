package dynamicFarm.processes

import dynamicFarm.records.ClassDefinitions
import dynamicFarm.records.InitialMessage
import dynamicFarm.records.ParseRecord
import dynamicFarm.records.Terminator
import dynamicFarm.records.VersionControl
import groovy_jcsp.ALT
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput
import jcsp.net2.NetChannel
import jcsp.net2.NetChannelInput
import jcsp.net2.NetChannelOutput
import jcsp.net2.mobile.CodeLoadingChannelFilter
import jcsp.net2.tcpip.TCPIPNodeAddress

class FarmManager implements CSProcess{
  ChannelOutput toE, toSP
  ChannelInput fromC, fromSP
  NetChannelInput fromNodes
  List <ParseRecord> structure
  ClassDefinitions classDefinitions
  String nature
  boolean verbose
  int portNumber = 56789 //port number used in TCPIP communications
  long firstNodeTime

  /**
   * This defines the actions of the process.*/
  @Override
  void run() {
    long startTime = System.currentTimeMillis()
    ALT controlAlt = new ALT([fromNodes, fromC])
    // now wait for a node to communicate
    boolean running, started
    (running,started) = [true, false]
    while (running){
      switch (controlAlt.select()) {
        case 0: //fromNodes
          if (!started) firstNodeTime = System.currentTimeMillis()
          // MUST only deal with a single node initialisation at a time
          InitialMessage startMessage = fromNodes.read ( ) as InitialMessage
          String nodeIP = startMessage.nodeIP
          String nodeVersion = startMessage.versionTag
          if (verbose) println "FarmManager: WorkNode $nodeIP has joined the farm "
          TCPIPNodeAddress nodeAddress
          nodeAddress = new TCPIPNodeAddress ( nodeIP, portNumber )
          NetChannelOutput toNode = NetChannel.one2net ( nodeAddress, 1, new CodeLoadingChannelFilter.FilterTX ( ) )
          // check version match
          if (nodeVersion  != VersionControl.versionTag) {
            println"Farmer is version ${VersionControl.versionTag}" +
                "\nbut WorkNode $nodeIP is running version $nodeVersion; they must be the same!"
            toNode.write(new Terminator())
            System.exit(-2)
          }
          println "Node on $nodeIP joined farm"

          // send the class definitions
          toNode.write(classDefinitions)

          // now put net output channel  information into the Map
          // create the node's net output channel to read buffer
          NetChannelOutput toNodeReadBuffer = NetChannel.one2net ( nodeAddress, 2 )
          if (verbose) println "FarmManager: updated active Nodes with net output channel ${toNodeReadBuffer.getLocation()} to node $nodeIP"

          // set up the new node by sending structure object to node
          toNode.write(structure)
          toSP.write(["T"])   //determine whether system is already terminating
          if (fromSP.read() as boolean){
            // node is being initialised after termination in the other nodes commenced
            toNode.write("ABORT")
          }
          else{
            // can start the node
            toSP.write(["U", nodeIP, toNodeReadBuffer])
            toNode.write("START")
          }
          // node will now start its internal processes by signalling Emit
          if ( !started ) {  // on first iteration set Emit process running
            if (verbose)println "FarmManager: starting Emit process"
            started = true
            toE.write ( "GO" as String )
          }
          break
        case 1: //fromC
//          println "FM: got Terminator"
          Terminator message = fromC.read() as Terminator
          assert (message.nodeIP == "STOP") : "FarmManager expected STOP from Collect process but received $message"
          running = false
      } // switch
    } //running
    long endTime = System.currentTimeMillis()
    println "FarmManager terminated: elapsed = ${endTime - startTime}; processing = ${endTime - firstNodeTime}"
  } // run
}
