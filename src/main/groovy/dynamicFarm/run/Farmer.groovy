package dynamicFarm.run

import dynamicFarm.processes.Collect
import dynamicFarm.processes.Emit
import dynamicFarm.processes.FarmManager
import dynamicFarm.processes.ReadResults
import dynamicFarm.processes.RequestManager
import dynamicFarm.processes.SendWork
import dynamicFarm.processes.SharedProperties
import dynamicFarm.records.ClassDefinitions
import dynamicFarm.records.ParseRecord
import dynamicFarm.records.VersionControl
import dynamicFarm.records.ExtractVersion
import groovy_jcsp.PAR
import jcsp.lang.CSProcess
import jcsp.lang.Channel
import jcsp.lang.One2OneChannel
import jcsp.net2.NetAltingChannelInput
import jcsp.net2.NetChannel
import jcsp.net2.Node
import jcsp.net2.tcpip.TCPIPNodeAddress

class Farmer {
  String fileBaseName
  Class dataClass
  Class sourceDataClass
  Class workDataClass
  Class resultClass
  String nature
  boolean verbose

  String structureFileName
  int portNumber = 56789

  // the Farmer simply gets parameter values passed to it and then starts the
  // processes on the Farmer host node.  The FarmManager process manages everything,
  // initially receiving parameter values from the Farmer

  /**
   *
   * @param fileBaseName the structure file name with no .df suffix
   * @param dataClass the class containing the object to be processed not null
   * @param sourceDataClass the data class used by Emit to access source data from an object file or null
   * @param workDataClass the class used for a node's shared data object or null, used by all nodes
   * @param resultClass the class used to collect the resulting toSW not null
   * @param nature either Net or Local; local means the Farmer runs on loopback node 127.0.0.1 and
   * the nodes must run om local loopback nodes 127.0.0.n (n !=1)
   * @param verbose if true prints debug toSW otherwise minimal process toSW
   */

  Farmer (
      String fileBaseName,
      Class dataClass,
      Class sourceDataClass,
      Class workDataClass,
      Class resultClass,
      String nature,
      boolean verbose ) {
    this.fileBaseName = fileBaseName
    this.dataClass = dataClass
    this.sourceDataClass = sourceDataClass
    this.workDataClass = workDataClass
    this.resultClass = resultClass
    this.nature = nature
    this.verbose = verbose
    this.structureFileName = fileBaseName + ".dfstruct"
  }

  void invoke() {
    println "Farm initiated using software version ${VersionControl.versionTag}"
    long startTime = System.currentTimeMillis()
    // read in the structure file
    File objFile = new File(structureFileName)
    List <ParseRecord> structure = []
    objFile.withObjectInputStream { inStream ->
      inStream.eachObject { structure << (it as ParseRecord) } }
    String parsedVersion = structure[0].version
    if (verbose) println "Using version $parsedVersion of the software"
    if (parsedVersion != VersionControl.versionTag){
      println "Version mismatch error:  Parser is version $parsedVersion\n" +
          "Software is version ${VersionControl.versionTag}"
      System.exit(-2)
    }
    // only tested ONCE the library is available!!
    if (!(ExtractVersion.extractVersion(parsedVersion))){
      println "The library dynamicFramework version $parsedVersion needs to be downloaded\n" +
          "Please modify the gradle.build file accordingly"
      System.exit(-2)
    }

    ClassDefinitions classDefs = new ClassDefinitions(
        dataClass: dataClass,
        resultClass: resultClass,
        sourceDataClass: sourceDataClass,
        workDataClass: workDataClass,
        version: parsedVersion
    )

    // now create the node and required Net Channels
    TCPIPNodeAddress farmerNodeAddress
    if (nature == "Net")
      farmerNodeAddress = new TCPIPNodeAddress( portNumber)  // find most global IP address available
    else
      farmerNodeAddress = new TCPIPNodeAddress( "127.0.0.1", portNumber)  // assumed local host IP
    Node.getInstance().init(farmerNodeAddress)
    String farmerIP = farmerNodeAddress.getIpAddress()
    if(verbose) println "FarmManager: farmer IP is $farmerIP"
    // create net input channel through which the nodes initiate communication
    NetAltingChannelInput fromNodes = NetChannel.numberedNet2One(1)
    // create net input channels: RBbynetRM and WBbynetRR
    NetAltingChannelInput RBbynetRM = NetChannel.numberedNet2One(2)
    NetAltingChannelInput WBbynetRR = NetChannel.numberedNet2One(3)
    if (verbose) println "Required Net Channels have been created"

    List < CSProcess> processes = []
    One2OneChannel FM2E  = Channel.one2one()
    One2OneChannel C2FM  = Channel.one2one()
    One2OneChannel E2SW  = Channel.one2one()
    One2OneChannel RR2C  = Channel.one2one()
    One2OneChannel SW2RM = Channel.one2one()
    One2OneChannel RM2SW = Channel.one2one()
    One2OneChannel E2SP  = Channel.one2one()
    One2OneChannel FM2SP = Channel.one2one()
    One2OneChannel SP2FM = Channel.one2one()
    One2OneChannel SW2SP = Channel.one2one()
    One2OneChannel SP2SW = Channel.one2one()
    One2OneChannel C2SP  = Channel.one2one()
    One2OneChannel RR2SP = Channel.one2one()
    One2OneChannel SP2RR = Channel.one2one()

    processes << new SharedProperties(
        fromE: E2SP.in(),
        fromFM: FM2SP.in(),
        fromSW: SW2SP.in(),
        fromRR: RR2SP.in(),
        fromC: C2SP.in(),
        toFM: SP2FM.out(),
        toSW: SP2SW.out(),
        toRR: SP2RR.out() )
    processes << new Emit(
        fromFM: FM2E.in(),
        toSW: E2SW.out(),
        toSP: E2SP.out(),
        classDef: dataClass,
        sourceDef: sourceDataClass,
        emitParams: structure[1].dataParameters,
        sourceDataFileName: structure[1].sourceDataFileName )
    processes << new SendWork(
        fromE: E2SW.in(),
        toRM: SW2RM.out(),
        fromRM: RM2SW.in(),
        toSP: SW2SP.out(),
        fromSP: SP2SW.in())
    processes << new RequestManager(
        fromRB: RBbynetRM,
        fromSW: SW2RM.in(),
        toSW: RM2SW.out())
    processes << new ReadResults(
        fromWB: WBbynetRR,
        toC: RR2C.out(),
        fromSP: SP2RR.in(),
        toSP: RR2SP.out()
    )
    processes << new Collect(
        fromRR: RR2C.in(),
        toFM: C2FM.out(),
        toSP: C2SP.out(),
        resultClass: resultClass,
        classParameters: structure[3].resultParameters,
        collectParameters: structure[3].collectParameters,
        finaliseParameters: structure[3].finaliseParameters  )
    processes << new FarmManager(
        classDefinitions: classDefs,
        structure: structure,
        fromNodes: fromNodes,
        toE: FM2E.out(),
        fromC: C2FM.in(),
        toSP: FM2SP.out(),
        fromSP: SP2FM.in(),
        nature: nature,
        verbose: verbose,
    )
    println "Farmer is starting processes"
    new PAR(processes).run()
    println "Farmer total elapsed time is ${System.currentTimeMillis() - startTime}"
  } // invoke
}
