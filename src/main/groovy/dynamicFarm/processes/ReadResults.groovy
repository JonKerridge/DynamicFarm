package dynamicFarm.processes


import dynamicFarm.records.Terminator
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput
import jcsp.net2.NetAltingChannelInput

class ReadResults implements CSProcess{
  NetAltingChannelInput fromWB
  ChannelOutput toC
  boolean running
  ChannelInput fromSP
  ChannelOutput toSP


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
            toSP.write(1)
            running = (!(fromSP.read() as boolean))
          }
    } // while running
    toC.write(new Terminator(nodeIP: "ReadResults"))
//    println "ReadResults: sent termination to collect and terminated itself"
  }
}
