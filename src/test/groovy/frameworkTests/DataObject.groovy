package frameworkTests


import dynamicFarm.records.DataInterface
import dynamicFarm.records.WorkDataInterface

class DataObject implements DataInterface<DataObject> {
  int value
  int initialValue, finalValue

  // values ranging from (initialValue + 1) to finalValue will be toSW

  DataObject(List params){
    initialValue = params[0] as int
    finalValue = params[1] as int
  }

  DataObject(int value){
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
  DataObject create(Object sourceData) {
    initialValue++
    if (initialValue <= finalValue) {
//      println "\t\t\t\t\t\tcreating new object with $initialValue"
      return new DataObject(initialValue)
    }
    else
      return null
  }


}
