package dynamicFarm.testSscripts

import oshi.SystemInfo
import oshi.hardware.CentralProcessor
import oshi.hardware.HardwareAbstractionLayer


SystemInfo systemInfo = new SystemInfo()
HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware()
CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor()

println " Cores = ${centralProcessor.getPhysicalProcessorCount()}"