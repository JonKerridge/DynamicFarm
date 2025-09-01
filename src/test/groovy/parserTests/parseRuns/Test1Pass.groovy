package parserTests.parseRuns

import dynamicFarm.parse.Parser
import groovy.test.GroovyTestCase

class Test1Pass  extends GroovyTestCase {

  void test(){
    String inFile = "D:/IJGradle/DynamicFarm/src/test/groovy/parserTests/DSLfiles/Test1"
    Parser parser = new Parser(inFile)
    boolean result = parser.parse()
    assertTrue ( result)
  }
}
