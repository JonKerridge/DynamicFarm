package dynamicFarm.records

class Acknowledgement implements Serializable{
  int ackValue
  String ackString

  Acknowledgement(int value, String string){
    ackValue = value
    ackString = string
  }

  String toString(){
    return "ACK value: $ackValue, string: $ackString"
  }
}
