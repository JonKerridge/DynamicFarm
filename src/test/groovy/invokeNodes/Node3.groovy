package invokeNodes


import dynamicFarm.run.WorkNode

class Node3 {
  static void main(String[] args) {
    new WorkNode("127.0.0.1", 2, "127.0.0.3", false).invoke()
  }
}
