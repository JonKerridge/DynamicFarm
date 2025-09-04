package frameworkTests

import dynamicFarm.records.ResultInterface

class ResultObject implements ResultInterface <DataObject>{

  int sum, count
  PrintWriter printWriter

  ResultObject(List p){
    String fileName = p[0]
    String path = "./data/${fileName}.txt"
    printWriter = new PrintWriter(path)
    this.sum = 0
    this.count = 0
  }

  @Override
  void collate(DataObject data, List params) {
//    println "\t\t\t\tCollected $data"
    count++
    sum = sum + data.value
    printWriter.println "$data"
    printWriter.flush()
  }

  @Override
  void finalise(List params) {
    println "Total sum = $sum from $count data points"
    printWriter.flush()
    printWriter.close()
  }
}
