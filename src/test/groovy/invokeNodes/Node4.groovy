package invokeNodes

import dynamicFarm.run.NodeRun

class Node4 {
  static void main(String[] args) {
    new NodeRun("127.0.0.1", 2, "127.0.0.4", false).invoke()
  }
}
