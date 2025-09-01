package areaPoiTests.testFiles

import dynamicFarm.parse.Parser
import groovy.test.GroovyTestCase

class Test1Parse extends GroovyTestCase {

  void test(){
    String inFile = "D:/IJGradle/DynamicFarm/src/test/groovy/areaPoiTests/testFiles/Test1"
    Parser parser = new Parser(inFile)
    boolean result = parser.parse()
    assertTrue ( result)
  }
}
