package com.mobeetest.worker.data.model.device

import com.mobeetest.worker.BuildConfig
import java.math.RoundingMode

data class DeviceInfo(
    val cpu: CpuInfo,
    val gpu: GpuInfo,
    val ram: RamInfo,
    val storage: List<StorageInfo>,
    val screen: ScreenInfo,
    val os: OsInfo,
    val hardware: HardwareInfo,
    val weatherInfo: String? = null, // ✅ new field
    val mobeetestInfo: MobeetestInfo
)

data class MobeetestInfo(
    val dateInstalled: Long,
    val dateLastUpdated: Long,
    val totalUpdates: Int,
    val version: String,
    val flavor: String,
    val fcmToken: String?,
    val fcmUuid: String?,
    val totalApplicationRunsSinceFirstInstall: Int,
    val ftpAvailableStorage: Long?,
    val totalActiveServices: Int,
    val ramPercentageUsage: Float?,
    val cpuPercentageUsage: Float?,

    val lastRunAt: Long? = null,                 // τελευταία φορά που μπήκε foreground η εφαρμογή
    val installerPackage: String? = null,        // π.χ. com.android.vending ή null για sideload
    val buildType: String = BuildConfig.BUILD_TYPE,            // π.χ. "debug" / "release"
    val applicationId: String = BuildConfig.APPLICATION_ID,
) {

    private val dayMillis = 24L * 60L * 60L * 1000L

    /**
     * Days since the app was first installed on this device.
     */
    val daysSinceInstall: Long
    get() = kotlin.math.max(
        0L,
        (System.currentTimeMillis() - dateInstalled) / dayMillis
    )

    /**
     * Average launches per day since first install.
     */
    val averageRunsPerDay: Float
    get() = if (daysSinceInstall <= 0L) {
        totalApplicationRunsSinceFirstInstall.toFloat()
    } else {
        totalApplicationRunsSinceFirstInstall.toFloat() / daysSinceInstall.toFloat()
    }

    /**
     * True if FCM appears correctly configured (both token and UUID present).
     */
    val isFcmConfigured: Boolean
    get() = !fcmToken.isNullOrBlank() && !fcmUuid.isNullOrBlank()

    /**
     * True if we have any FTP storage info available.
     */
    val hasFtpInfo: Boolean
    get() = ftpAvailableStorage != null
}

data class CpuInfo(
    val numberOfCores: Int,
    val maxFrequenciesMHz: List<Int>,
    val socName: String,
    val abi: String,
    val armNeon: Boolean,
    val l1dCache: List<String>,
    val l1iCache: List<String>,
    val l2Cache: List<String>,
    val l3Cache: List<String>
)

data class GpuInfo(
    val vulkanVersion: String,
    val glesVersion: String,
    val vendor: String,
    val renderer: String,
    val extensions: List<String>
)

data class RamInfo(
    val totalMB: Float,
    val availableMB: Float,
    val availablePercent: Float,
    val thresholdMB: Float
)

data class StorageInfo(
    val label: String,
    val totalBytes: Long,
    val usedBytes: Long,
    val path: String
) {
    val percentUsed: Double
        get() = if (totalBytes > 0) {
            (usedBytes * 100.0 / totalBytes)
                .toBigDecimal()
                .setScale(3, RoundingMode.HALF_UP)
                .toDouble()
        } else {
            0.0
        }
}


data class ScreenInfo(
    val screenClass: String,
    val densityClass: String?,
    val widthPx: Int,
    val heightPx: Int,
    val dpWidth: Int,
    val dpHeight: Int,
    val density: Float,
    val absoluteWidthPx: Int,
    val absoluteHeightPx: Int,
    val absoluteDpWidth: Int,
    val absoluteDpHeight: Int,
    val refreshRateHz: Float,
    val orientation: Int
)

data class OsInfo(
    val osName: String,
    val version: String,
    val sdk: Int,
    val codename: String,
    val bootloader: String,
    val brand: String,
    val model: String,
    val manufacturer: String,
    val board: String,
    val vm: String,
    val kernel: String,
    val serial: String,
    val language: String,
    val androidId: String,
    val isRooted: Boolean,
    val encryptedStorage: String,
    val strongBox: String,
    val fingerprint: String,
    val supportedAbis: List<String>,
    val isEmulator: Boolean,
    val miuiVersion: String?,
    val fcmToken: String?,
    val fcmUuid: String?,
    val securityProviders: Map<String, String>
)

data class HardwareInfo(
    val battery: BatteryInfo,
    val cameras: List<CameraInfo>,
    val soundCardCount: Int,
    val wireless: WirelessInfo,
    val usb: UsbInfo
)

data class BatteryInfo(
    val levelPercent: Int?,              // current level in percent, null if unknown
    val health: BatteryHealth?,          // health status if available
    val chargerConnection: ChargerConnection?, // AC / USB / WIRELESS / NONE / UNKNOWN
    val status: BatteryStatus?,          // charging state
    val temperatureC: Float?,            // battery temperature in °C, null if unknown
    val capacityMah: Float,              // nominal capacity from PowerProfile
    val technology: String               // Li-ion, Li-polymer, etc.
)

enum class BatteryHealth {
    GOOD,
    FAILED,
    DEAD,
    OVERVOLTAGE,
    OVERHEATED,
    UNKNOWN
}

enum class ChargerConnection {
    AC,
    USB,
    WIRELESS,
    NONE,
    UNKNOWN
}

enum class BatteryStatus {
    UNKNOWN,
    CHARGING,
    DISCHARGING,
    NOT_CHARGING,
    FULL
}

data class CameraInfo(
    val type: String, // e.g., "Front", "Back"
    val orientation: Int
)

data class WirelessInfo(
    val bluetooth: Boolean,
    val bluetoothLE: Boolean,
    val gps: Boolean,
    val nfc: Boolean,
    val nfcCardEmulation: Boolean,
    val wifi: Boolean,
    val wifiAware: Boolean,
    val wifiDirect: Boolean,
    val wifiPasspoint: Boolean,
    val wifi5Ghz: Boolean,
    val wifiP2p: Boolean,
    val irEmitter: Boolean
)

data class UsbInfo(
    val otg: Boolean
)


