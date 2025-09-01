package dynamicFarm.processes

import dynamicFarm.records.EmitInterface
import dynamicFarm.records.Terminator
import dynamicFarm.records.WorkDataInterface
import dynamicFarm.records.ExtractParameters
import jcsp.lang.CSProcess
import jcsp.lang.ChannelInput
import jcsp.lang.ChannelOutput

class Worker implements CSProcess{

  ChannelOutput toReadBuffer
  ChannelInput fromReadBuffer
  ChannelOutput toWriteBuffer
  int workerIndex
  List workParam
  String methodName, nodeIP
  Class workDataClass
  String workDataFileName
  WorkDataInterface workData


  @Override
  void run() {
    boolean running
//    println "Worker [$nodeIP:$workerIndex] running using $workParam"
    List parameterValues = ExtractParameters.extractParams(workParam[0] as List, workParam[1] as List)
//    println "Worker [$nodeIP:$workerIndex] running using parameters: $parameterValues"
    running = true
    while (running){
//      println "Worker [$nodeIP:$workerIndex] sending request to RB"
      toReadBuffer.write(workerIndex)
//      println "Worker [$nodeIP:$workerIndex] sent request"
      def object = fromReadBuffer.read()
//      println "Worker [$nodeIP:$workerIndex] received response $object"
      if (object instanceof Terminator)
        running = false
      else {
        (object as EmitInterface).&"$methodName"(workData, parameterValues)
        toWriteBuffer.write(object)
//        println "Worker [$nodeIP:$workerIndex] sent toSW $object to WB"
      }
    } // running
//    println "Worker [$nodeIP:$workerIndex] has stopped running"
    toWriteBuffer.write(new Terminator(
        nodeIP: nodeIP
      )
    )
//    println "Worker [$nodeIP:$workerIndex] terminated "

  }
}
