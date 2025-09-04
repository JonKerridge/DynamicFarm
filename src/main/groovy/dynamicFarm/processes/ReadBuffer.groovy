package dynamicFarm.processes


import dynamicFarm.records.DataInterface
import dynamicFarm.records.RequestWork
import dynamicFarm.records.Terminator
import groovy_jcsp.ALT
import groovy_jcsp.ChannelOutputList
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannelOutput

class ReadBuffer implements CSProcess{

  //external channels - connections to SendWork in Controller
  NetChannelOutput readyToRead
  NetAltingChannelInput objectInput
  //internal channels
  ChannelOutputList toInternalProcesses
  ChannelInput fromInternalProcesses  //a shared channel input
  int nInternals
  String nodeIP

  @Override
  void run() {
    RequestWork getWork = new RequestWork(nodeIP: nodeIP)
//    println "Read Buffer starting $nInternals  internal processes"
    List buffer = new List[nInternals]
    boolean running
    int readFrom, writeTo, entries  //circular buffer indices and counts
    ALT readAlt = new ALT([objectInput, fromInternalProcesses])
    (readFrom, writeTo, entries) = [0,0,0]
    running = true
    boolean [] preCon
//    println "ReadBuffer [$nodeIP]: starting with $nInternals Worker processes"
    // pre-fill the internal buffer
    for (i in 0 ..< nInternals){
      readyToRead.write(getWork)
    }
//    println"ReadBuffer[$nodeIP]: has prefilled its buffer"
    while (running){
      preCon = [(entries < nInternals), entries > 0]
//      println "RB[$nodeIP]: preCon - $preCon; [fromSend, fromWorker] entries is $entries of $nInternals"
      switch (readAlt.priSelect(preCon)){
        case 0: // object input from SendWork
          def object = objectInput.read()
          if (!(object instanceof Terminator)) {
//            println "RB[$nodeIP]: has read $object"
            buffer[writeTo] = object as DataInterface
//            println "RB[$nodeIP]: read ${buffer[writeTo]} into buffer[$writeTo]"
            writeTo = (writeTo + 1) % nInternals
            entries = entries + 1
          } else {  //stop the running loop Terminator read
            // have to inform RequestManager as well
            readyToRead.write(new Terminator(nodeIP: nodeIP))
            running = false
//            println "RB[$nodeIP]: terminating $running"
          }
          break
        case  1:  //request from one of the internal processes
          int internalIndex = fromInternalProcesses.read() as int
//          println "RB[$nodeIP] : received request from Worker $internalIndex"
          toInternalProcesses[internalIndex].write(buffer[readFrom])
//          println "RB[$nodeIP] : written ${buffer[readFrom]} from buffer[$readFrom] to Worker[$internalIndex]"
          readFrom = (readFrom + 1) % nInternals
          entries = entries - 1
          // must accept requests from internal processes while terminating as there
          // could be valid data in buffer still to be processed
          readyToRead.write(getWork)
          break
      } // switch
    } // running
//    println "Read Buffer[$nodeIP]: termination phase"
    // node terminating but could still be entries in local buffer
    while (entries > 0) {
      int internalIndex = fromInternalProcesses.read() as int
      toInternalProcesses[internalIndex].write(buffer[readFrom])
//          println "RB[$nodeIP]: terminating written ${buffer[readFrom]} in $readFrom"
      readFrom = (readFrom + 1) % nInternals
      entries = entries - 1
    }
//    println "Read Buffer: emptied local buffer"
    // local buffer is now empty so terminate the internal processes
    for ( i in 0 ..< nInternals){
      int internalIndex = fromInternalProcesses.read() as int
//      println "Read Buffer[$nodeIP]: terminating internal $internalIndex"
      toInternalProcesses[internalIndex].write(new Terminator())
    }
  } // run()
}
