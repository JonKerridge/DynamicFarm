package dynamicFarm.run

import dynamicFarm.processes.ReadBuffer
import dynamicFarm.processes.Worker
import dynamicFarm.processes.WriteBuffer
import dynamicFarm.records.ClassDefinitions
import dynamicFarm.records.InitialMessage
import dynamicFarm.records.ParseRecord
import dynamicFarm.records.Terminator
import dynamicFarm.records.VersionControl
import dynamicFarm.records.WorkDataInterface
import groovy_jcsp.ChannelOutputList
import groovy_jcsp.PAR
import jcsp.lang.Any2OneChannel
import jcsp.lang.CSProcess
import jcsp.lang.CSTimer
import jcsp.lang.Channel
import jcsp.lang.One2OneChannel
import jcsp.net2.mobile.CodeLoadingChannelFilter
import jcsp.net2.tcpip.TCPIPNodeAddress
import jcsp.net2.*
import oshi.SystemInfo
import oshi.hardware.CentralProcessor
import oshi.hardware.HardwareAbstractionLayer

/**
 * The code that invokes a worker node and its internal processes
 *
 * The host node will complete all the interaction phases with one node
 * BEFORE attempting to initiate another worker node
 */
class NodeRun {

  String hostIP, localIP
  String version = VersionControl.versionTag
  int portNumber = 56789  //port number used in TCPIP communications
  int workers
  boolean verbose

  //initialisation channels
  NetChannelOutput registerNode
  NetChannelInput goSignal

  //communication channels
  NetAltingChannelInput objectInput
  NetChannelOutput readyToRead, outputToResults

/**
 * Invoke a node
 * @param hostIP the IP address of the host
 * @param workers the number of worker processes in this node, if zero
 * the number of available processors (cores) will be determined dynamically
 */
  NodeRun(String hostIP, int workers){
    this.hostIP = hostIP
    this.localIP = null
    this.workers = workers
    verbose = false
  }

/**
 * Invoke a node
 * @param hostIP the IP address of the host
 * @param workers the number of worker processes in this node, if zero
 * the number of available processors (cores) will be determined dynamically
 * @param verbose defaults to false but when true causes debug toSW
 */
  NodeRun(String hostIP, int workers, boolean verbose){
    this.hostIP = hostIP
    this.localIP = null
    this.workers = workers
    this.verbose = verbose
  }

  /**
   * Invoke a node in Local mode
   * @param hostIP the host IP address, usually 127.0.0.1
   * @param localIP the Ip address of the node to be created
   * @param workers the number of worker processes in this node, workers must be non-zero
   */
  NodeRun(String hostIP, int workers, String localIP){
    this.hostIP = hostIP
    this.localIP = localIP
    this.workers = workers
    verbose = false
  }

  /**
   * Invoke a node in Local mode
   * @param hostIP the host IP address, usually 127.0.0.1
   * @param localIP the Ip address of the node to be created
   * @param workers the number of worker processes in this node, workers must be non-zero
   * @param verbose defaults to false but when true causes debug toSW
   */
  NodeRun(String hostIP, int workers, String localIP, boolean verbose){
    this.hostIP = hostIP
    this.localIP = localIP
    this.workers = workers
    this.verbose = verbose
  }

  void invoke() {
    // to ensure nodes wait 1 seconds until host has started
    CSTimer timer = new CSTimer()
    timer.sleep(1000)
    long startTime
    startTime = System.currentTimeMillis()
    List<ParseRecord> structure
    //Phase 1 send nodeLoader IP to host, and get classes and app structure

    // create this nodeLoader and make connections to and from host
    TCPIPNodeAddress nodeAddress
    if (localIP == null)
      nodeAddress = new TCPIPNodeAddress(portNumber) // most global IP address
    else
      nodeAddress = new TCPIPNodeAddress(localIP,portNumber)
    Node.getInstance().init(nodeAddress)
    String nodeIP = nodeAddress.getIpAddress()
    if (workers == 0){
      SystemInfo systemInfo = new SystemInfo()
      HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware()
      CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor()
      workers = centralProcessor.getPhysicalProcessorCount() - 1
      // allocate available cores less 1 for the Read and Write Buffer processes to share
    }
    println "NodeRun $nodeIP has started with host $hostIP" +
        " on port $portNumber using version $version and $workers worker processes"
    NetAltingChannelInput fromFarmer = NetChannel.numberedNet2One(1, new CodeLoadingChannelFilter.FilterRX())
    TCPIPNodeAddress hostAddress = new TCPIPNodeAddress(hostIP, portNumber)
    // the connection will fail id host already terminated
    NetSharedChannelOutput toFarmer
    toFarmer = null
    try {
      toFarmer = NetChannel.any2net(hostAddress, 1)
    }
    catch (JCSPNetworkException e){
      println "Node started after the system is already terminating - $e"
      System.exit((-1))
    }
    if (verbose) println "NodeRun $nodeIP has toFarmer channel ${toFarmer.getLocation()}" +
        "\n fromFarmer channel ${fromFarmer.getLocation()}"
    toFarmer.write(new InitialMessage(nodeIP: nodeIP, versionTag: version))
    // now get class definitions from Farmer or terminate if version mismatch
    Object dataFromFarmer
    dataFromFarmer = fromFarmer.read()
    if (dataFromFarmer instanceof Terminator){
      println "Host Terminated due to software mismatch between host and node"
      System.exit(-2)  // instant termination
    }

    ClassDefinitions classDefinitions = ( dataFromFarmer as ClassDefinitions)
    // create net  channels for this node
    // host will have already created its net input channels for this node
    NetChannelOutput requestWork, writeResults
    NetChannelInput getWork = NetChannel.numberedNet2One(2)
    requestWork = null
    writeResults = null
    try {
      requestWork = NetChannel.one2net(hostAddress, 2)
      writeResults = NetChannel.one2net(hostAddress, 3)
    }
    catch(JCSPNetworkException e){
      println "NetChannel creation failed - $e"
      System.exit((-1))
    }
    if (verbose) {
      println "NodeRun getWork channel: ${getWork.getLocation()}"
      println "NodeRun requestWork channel: ${requestWork.getLocation()}"
      println "NodeRun writeResults channel: ${writeResults.getLocation()}"
      println "NodeRun $nodeIP has completed net channel creation"
    }

// nodeLoader can now read in the structure object unless some preAllocated nodes have not been started

    try {
      dataFromFarmer = fromFarmer.read()
    }
    catch(Exception e){
      println "Error:$e occured during structure loading from Farmer to Node"
      System.exit(-1)
    }

    structure = (dataFromFarmer as List<ParseRecord>)
    if (verbose) structure.each {println "$it"}
    if (verbose) println "NodeRun $nodeIP has completed initial interaction and class loading"
    //load any work data required
    WorkDataInterface workData
    workData = null
    String workDataFileName = structure[2].workDataFileName
    Class workDataClass = classDefinitions.workDataClass
    if ( (workDataClass != null) && (workDataFileName != null)){
      Class[] cArg = new Class[1]
      cArg[0] = String.class
      workData = workDataClass.getDeclaredConstructor(cArg).newInstance(workDataFileName) as WorkDataInterface
      println "NodeRun $nodeIP has loaded $workDataFileName as work data"
    }
    else{
      if ( !((workDataFileName == null) && (workDataClass == null))){
        // there is a problem because either both must be null or both not null (see above)
        println "Inconsistency in source data specification" +
            "\nwork data file name is $workDataFileName and work data class name id ${workDataClass.getName()}" +
            "\neither both must be null or both must have values"
        System.exit(-3)
      }
      println "NodeRun $nodeIP has not loaded any local data"
    }
    if (verbose) println "NodeRun $nodeIP is creating node processes"
    // create internal channels
    Any2OneChannel workToReadBuffer = Channel.any2one()
    Any2OneChannel workToWriteBuffer = Channel.any2one()
    One2OneChannel[] readBufferToWork = Channel.one2oneArray(workers)
    ChannelOutputList read2work = new ChannelOutputList(readBufferToWork)
    List <CSProcess> nodeProcesses = []
    nodeProcesses << new ReadBuffer(
        readyToRead: requestWork,
        objectInput: getWork,
        toInternalProcesses: read2work,
        fromInternalProcesses: workToReadBuffer.in(),
        nInternals: workers,
        nodeIP: nodeIP   )
    for ( i in 0 ..< workers)
      nodeProcesses << new Worker(
          toReadBuffer: workToReadBuffer.out(),
          fromReadBuffer: readBufferToWork[i].in(),
          toWriteBuffer: workToWriteBuffer.out(),
          workerIndex: i,
          workParam: structure[2].workParameters,
          methodName: structure[2].workMethodName,
          nodeIP: nodeIP,
          workData: workData
      )
      nodeProcesses << new WriteBuffer(
        outputToResults: writeResults,
        fromInternals: workToWriteBuffer.in(),
        nInternals: workers,
        nodeIP: nodeIP
      )
    String message = fromFarmer.read()
    if (message == "START") {
      println"Node $nodeIP starting"
      new PAR(nodeProcesses).run()
      println "NodeRun $nodeIP total elapsed time = ${System.currentTimeMillis() - startTime} milliseconds"
    }
    else {
      println "$message - Node terminating as system termination already started"
      writeResults.write(new Terminator(nodeIP: nodeIP))
    }
    // tell FarmManager node has terminated
  } //invoke
}
