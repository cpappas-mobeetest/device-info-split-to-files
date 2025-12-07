package com.mobeetest.worker.utils.device

class CpuDataNativeProvider {
    external fun initLibrary()
    external fun getCpuName(): String
    external fun hasArmNeon(): Boolean
    external fun getL1dCaches(): IntArray?
    external fun getL1iCaches(): IntArray?
    external fun getL2Caches(): IntArray?
    external fun getL3Caches(): IntArray?
    external fun getNumberOfCores(): Int

    companion object {
        init {
            System.loadLibrary("cpuinfo-lib") // Must match add_library name in CMakeLists.txt
        }
    }
}
