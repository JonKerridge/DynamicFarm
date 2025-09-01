package dynamicFarm.parse

import dynamicFarm.records.ExtractVersion
import dynamicFarm.records.ParseRecord
import dynamicFarm.records.VersionControl
import groovyjarjarpicocli.CommandLine

/**
 * The class used to process a description of the processing required using
 * the cluster_framework_2 domain specific language.
 *
 * The class has a single method called parse()
 */
class Parser {

  String inputFileName, outputTextFile, outObjectFile
  String version = VersionControl.versionTag
/**
 * Create an instance of the parser
 * @param inFileName the full path name of the file containing the DSL specification,
 * excluding the suffix (.df)
 */
  Parser(String inFileName) {
    this.inputFileName = inFileName + ".df"
    outputTextFile = inputFileName + "txt"
    outObjectFile = inputFileName + "struct"
  }

  String hostIPAddress = "not set yet"

  class VersionSpecification {
    @CommandLine.Parameters( description = "the software version number") String versionString
  } // VersionSpecification

  class DataSpecification {
    @CommandLine.Option( names = "-p", split = "!") List<String> dataParamString
    @CommandLine.Option(names = ["-f", "-file"]) String sourceDataFileName
  } // DataSpecification

  class WorkSpecification {
    @CommandLine.Option(names = ["-m", "-method"], description = "name of work method used in this cluster") String workMethodName
    @CommandLine.Option( names = "-p", split = "!") List<String> workParamString
    @CommandLine.Option(names = ["-f", "-file"], description = "full filename of shared data")String workDataFileName
  } // WorkSpecification

  class ResultSpecification {
    @CommandLine.Option( names = "-p", split = "!") List<String> classParamString   // class parameter string
    @CommandLine.Option( names = "-cp", split = "!") List<String> collectParamString  // parameters of the collect method
    @CommandLine.Option( names = "-fp", split = "!") List<String> finaliseParamString  // parameters of the finalise method
  } // ResultSpecification

  // all the *ParamString properties will comprise 2 lists, the first is a comma separated list of data types,
  // the second is a list of values for each of the types specified in the same order.  Both strings are the same length.

  List errorMessages = []

  boolean checkValidity (boolean test, String message){
    if (test)
      return true
    else {
      println "$message"
      errorMessages << message
      return false
    }
  }
/**
 * Used to invoke the parsing of the input file
 *
 * @return indicates whether the parsing was successful or not
 */
  boolean parse(){
    int emitterProcesses, workClusters
    workClusters = 0
    boolean outcome
    outcome = true
    if (!ExtractVersion.extractVersion(version)){
      println "dynamicFarm: Version $version needs to be downloaded, please modify the gradle.build file"
//      System.exit(-1)
    }
    List<ParseRecord> buildData
    buildData = []
    new File(inputFileName).eachLine{ String inLine ->
      List<String> tokens = inLine.tokenize()
      String lineType = tokens.pop()
      String[] args = tokens.toArray(new String[0])
      ParseRecord parseRecord = new ParseRecord()
      switch (lineType) {
        case 'version':
          VersionSpecification versionSpecification = new VersionSpecification()
          new CommandLine(versionSpecification).parseArgs(args)
          println "Version = ${versionSpecification.versionString}"
          parseRecord.typeName = lineType
          parseRecord.version = versionSpecification.versionString
          assert version == parseRecord.version:"Version mismatch - Software is $version, specification expecting ${versionSpecification.versionString}"
          buildData << parseRecord
          break
        case 'data':
          DataSpecification data = new DataSpecification()
          new CommandLine(data).parseArgs(args)
//          println "Data:  Params = ${data.dataParamString}, source file =${data.sourceDataFileName }"
          if (data.dataParamString != null) {
            if (!checkValidity((data.dataParamString.size() == 2),
                "Data must have 2 parameter strings; ${data.dataParamString.size()} supplied ")) outcome = false
            else {
              // deal with any parameter string associated with source data
              data.dataParamString.each { String paramSpec ->
                List<String> tokenizedParams
                tokenizedParams = paramSpec.tokenize(',')
                parseRecord.dataParameters << tokenizedParams
                // this will comprise 2 entries both of which are lists
              }
            }
          }
          else {
            parseRecord.dataParameters = null
          }
          parseRecord.typeName = lineType
          parseRecord.hostAddress = hostIPAddress
          parseRecord.version = version
          parseRecord.sourceDataFileName = data.sourceDataFileName
          println "$parseRecord"
          buildData << parseRecord
          break

        case 'work':
          WorkSpecification work = new WorkSpecification()
          new CommandLine(work).parseArgs(args)
//          println "Work: Method = ${work.workMethodName}, Params = ${work.workParamString}, work file = ${work.workDataFileName}"
          parseRecord.typeName = lineType
          parseRecord.hostAddress = hostIPAddress
          parseRecord.version = version
          parseRecord.workDataFileName = work.workDataFileName  // could be null
          parseRecord.workMethodName = work.workMethodName
          if (work.workParamString != null){
            if (!checkValidity((work.workParamString.size() == 2),
                "Work: The parameter string must consist of ONE type list followed by ONE value list"))
              outcome = false
            work.workParamString.each { String paramSpec ->
              List<String> tokenizedParams
              tokenizedParams = paramSpec.tokenize(',')
              parseRecord.workParameters << tokenizedParams
              // there will be two lists in the record
            }
          }
          else parseRecord.workParameters = null
          println "$parseRecord"
          buildData << parseRecord
          break

        case 'result':
          ResultSpecification collect = new ResultSpecification()
          new CommandLine(collect).parseArgs(args)
//          println "Collect:  OutFile = ${collect.outFileName}, " +

          if (collect.classParamString != null) {
            if (!checkValidity((collect.classParamString.size() == 2),
                "Collect: The class constructor must have 2 parameter strings; ${collect.classParamString.size()} supplied"))
              outcome = false
            else{
              collect.classParamString.each { String paramSpec ->
                List<String> tokenizedParams
                tokenizedParams = paramSpec.tokenize(',')
                parseRecord.resultParameters << tokenizedParams
              }
            }
          }
          else parseRecord.resultParameters = null
          if (collect.collectParamString != null){
            if (!checkValidity((collect.collectParamString.size() == 2),
                "Collect: The collate method must have 2 parameter strings; ${collect.collectParamString.size()} supplied"))
              outcome = false
            else{
              collect.collectParamString.each { String paramSpec ->
                List<String> tokenizedParams
                tokenizedParams = paramSpec.tokenize(',')
                parseRecord.collectParameters << tokenizedParams
              }
            }
          }
          else parseRecord.collectParameters = null
          if (collect.finaliseParamString != null){
            if (!checkValidity((collect.finaliseParamString.size() == 2),
                "Collect: The finalise method must have 2 parameter strings; ${collect.finaliseParamString.size()} supplied"))
              outcome = false
            else{
              collect.finaliseParamString.each { String paramSpec ->
                List<String> tokenizedParams
                tokenizedParams = paramSpec.tokenize(',')
                parseRecord.finaliseParameters << tokenizedParams
              }
            }
          }
          else parseRecord.finaliseParameters = null
          parseRecord.typeName = lineType
          parseRecord.hostAddress = hostIPAddress
          parseRecord.version = version
          println "$parseRecord"
          buildData << parseRecord
          break
        default:
          println "$lineType incorrectly specified"
          outcome = false
          break
      }
    }  // file each line
// now do the toSW
      File outFile = new File(outputTextFile)
      PrintWriter printWriter = outFile.newPrintWriter()
      buildData.each { printWriter.println "$it" }
      if (!outcome)
        errorMessages.each {printWriter.println "$it" }
      printWriter.flush()
      printWriter.close()
      File outObjFile = new File(outObjectFile)
      ObjectOutputStream outStream = outObjFile.newObjectOutputStream()
      buildData.each { outStream << it }
      outStream.flush()
      outStream.close()
    if (outcome)
      println "Parsing completed - no errors in $inputFileName"
    else
      println "Parsing failed, see errors highlighted above in $inputFileName"
    return outcome
  }// parse
}
