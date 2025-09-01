package dynamicFarm.records

class ClassDefinitions implements Serializable{
  Class dataClass  // the class associated with the emit process
  Class resultClass // the class associated with the collect process
  Class sourceDataClass // added v2 so source data can come from file
  Class workDataClass
  String version  // the version must match for Parser and Host and Nodes
}
