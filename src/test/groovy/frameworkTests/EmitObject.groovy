package frameworkTests

import dynamicFarm.records.EmitInterface
import dynamicFarm.records.SourceDataInterface
import dynamicFarm.records.WorkDataInterface

class EmitObject implements EmitInterface<EmitObject>{
  int value
  int initialValue, finalValue

  // values ranging from (initialValue + 1) to finalValue will be toSW

  EmitObject(List params){
    initialValue = params[0] as int
    finalValue = params[1] as int
  }

  EmitObject(int value){
    this.value = value
    this.initialValue = 0
    this.finalValue = 0
  }

  void updateMethod(WorkDataInterface wd, List params){
//    println "\t\t\t\t\tprocessing object $value"
    value = value + (params[0] as int)
  }

  void updateMethod2(WorkDataInterface wd, List params){
    int multiplier
    multiplier = (int) (value / (1000 as int))
    value = value - (multiplier * (1000 as int))
  }

  String toString(){
    return "$value"
  }

  @Override
  EmitObject create(Object sourceData) {
    initialValue++
    if (initialValue <= finalValue) {
//      println "\t\t\t\t\t\tcreating new object with $initialValue"
      return new EmitObject(initialValue)
    }
    else
      return null
  }


}
