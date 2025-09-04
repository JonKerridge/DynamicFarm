package frameworkTests

import dynamicFarm.run.Farmer

class T1Farmer {
  static void main(String[] args) {
    String structure =  "D:/IJGradle/DynamicFarm/src/test/groovy/parserTests/DSLfiles/Test1"
    Class dataClass = DataObject
    Class resultClass = ResultObject
    new Farmer( structure, dataClass, null, null,
        resultClass,"Local", true ).invoke()
  }
}
