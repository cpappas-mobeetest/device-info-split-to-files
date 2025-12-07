package com.mobeetest.worker.viewModels

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.*
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mobeetest.worker.MobeetestApp
import com.mobeetest.worker.utils.device.CpuDataNativeProvider
import com.mobeetest.worker.utils.device.HeadlessGpuInfoFetcher
import kotlinx.coroutines.launch
import java.io.*
import java.security.Security
import java.util.*
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.content.IntentFilter
import android.hardware.ConsumerIrManager
import android.net.wifi.WifiManager
import com.mobeetest.worker.data.model.device.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URL

//import java.net.URL


import android.Manifest
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.system.Os
import android.system.OsConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import com.mobeetest.worker.BuildConfig
/*import com.mobeetest.worker.services.cellular.CellularService
import com.mobeetest.worker.services.location.LocationService
import com.mobeetest.worker.services.network.NetworkService
import com.mobeetest.worker.services.network_drive.NetworkDriveService
import com.mobeetest.worker.services.notification.NotificationService
import com.mobeetest.worker.services.sensors.SensorsService
import com.mobeetest.worker.services.speed_test.SpeedTestService
import com.mobeetest.worker.services.wiFi.WiFiService
import com.mobeetest.worker.services.wiFi_drive.WiFiDriveService
 */
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.Long
import kotlin.coroutines.resume

@ExperimentalFoundationApi
class DeviceInfoViewModel(application: Application) : AndroidViewModel(application), KoinComponent {


    private val _deviceInfo = MutableStateFlow<DeviceInfo? >(null)
    val deviceInfo: StateFlow<DeviceInfo? > = _deviceInfo.asStateFlow()

    private val _updateInProgress = MutableStateFlow(false)
    val updateInProgress: StateFlow<Boolean> = _updateInProgress.asStateFlow()

    private val wifiManager: WifiManager by inject()
    private val cpuNativeProvider = CpuDataNativeProvider(). apply { initLibrary() }
    private val cpuMonitor = CpuMonitor() // ✅ Already declared
    private val debugLogging = false

    fun collectDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            cpu = getCpuInfo(),
            gpu = getGpuInfo(),
            ram = getRamInfo(),
            storage = getStorageInfo(getApplication()),
            screen = getScreenInfo(),
            os = getOsInfo(),
            hardware = getHardwareInfo(),
            weatherInfo = "",
            mobeetestInfo = getMobeetestInfoWithoutCpu() // ✅ Now suspend
        )
    }

    /*
    private fun getTotalActiveServices(context: Context): Int {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 0

        @Suppress("DEPRECATION")
        val running = activityManager.getRunningServices(Int.MAX_VALUE)

        if (running.isEmpty()) return 0

        val targetClasses = setOf(
            SensorsService::class.java.name,
            LocationService::class.java.name,
            NetworkService::class.java.name,
            NetworkDriveService::class.java.name,
            CellularService::class.java.name,
            SpeedTestService::class.java.name,
            WiFiService::class.java.name,
            WiFiDriveService::class.java.name,
            NotificationService::class.java.name
        )

        return running
            .map { it.service.className }
            .distinct()
            .count { it in targetClasses }
    }
     */



    private fun getMobeetestInfoWithoutCpu(): MobeetestInfo {
        val context = getApplication<Application>()
        val packageManager = context.packageManager
        val packageName = context.packageName

        // Backward-compatible getPackageInfo
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }

        val dateInstalled = packageInfo.firstInstallTime
        val dateLastUpdated = packageInfo.lastUpdateTime
        val versionName = packageInfo.versionName ?: "unknown"

        val totalRuns = try {
            val prefs = context.getSharedPreferences("mobeetest_app_stats", Context.MODE_PRIVATE)
            prefs.getInt("total_runs", 0)
        } catch (_: Exception) {
            0
        }

        val lastRunAt = try {
            val prefs = context.getSharedPreferences("mobeetest_app_stats", Context.MODE_PRIVATE)
            if (prefs.contains("last_run_at")) prefs.getLong("last_run_at", 0L) else null
        } catch (_: Exception) {
            null
        }

        val totalUpdates = if (dateLastUpdated > dateInstalled) {
            1 // simple heuristic for now
        } else {
            0
        }

        val installerPackage: String? = try {
            packageManager.getInstallSourceInfo(packageName).installingPackageName
        } catch (_: Exception) {
            null
        }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo().also {
            activityManager?.getMemoryInfo(it)
        }

        val totalRam = memoryInfo.totalMem
        val availRam = memoryInfo.availMem
        val usedRam = (totalRam - availRam).coerceAtLeast(0L)
        val ramPercentage = if (totalRam > 0L) {
            (usedRam.toDouble() / totalRam.toDouble() * 100.0).toFloat()
        } else {
            null
        }

        //val runtime = Runtime.getRuntime()
        val ftpAvailableStorage = try {
            val statFs = StatFs(context.filesDir.absolutePath)
            statFs.availableBytes
        } catch (_: Throwable) {
            0L
        }

        return MobeetestInfo(
            dateInstalled = dateInstalled,
            dateLastUpdated = dateLastUpdated,
            totalUpdates = totalUpdates,
            version = versionName,
            flavor = "free",
            fcmToken = "",   // to be filled when FCM is plugged
            fcmUuid = "",    // to be filled when FCM is plugged
            totalApplicationRunsSinceFirstInstall = totalRuns,
            ftpAvailableStorage = ftpAvailableStorage,
            totalActiveServices = 0, // can be wired later
            ramPercentageUsage = ramPercentage,
            cpuPercentageUsage = null, // filled by the CPU monitor if available
            lastRunAt = lastRunAt,
            installerPackage = installerPackage,
            buildType = BuildConfig.BUILD_TYPE
        )
    }



    private fun getCpuInfo(): CpuInfo {
        fun formatSizes(sizes: IntArray?): List<String> = sizes?.map { "${it / 1024}KB" } ?: emptyList()

        return CpuInfo(
            numberOfCores = cpuNativeProvider.getNumberOfCores(),
            maxFrequenciesMHz = getCpuFrequenciesMHz(),
            socName = cpuNativeProvider.getCpuName(),
            abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
            armNeon = cpuNativeProvider.hasArmNeon(),
            l1dCache = formatSizes(cpuNativeProvider.getL1dCaches()),
            l1iCache = formatSizes(cpuNativeProvider.getL1iCaches()),
            l2Cache = formatSizes(cpuNativeProvider.getL2Caches()),
            l3Cache = formatSizes(cpuNativeProvider.getL3Caches())
        )
    }

    private fun getCpuFrequenciesMHz(): List<Int> {
        val cores = Runtime.getRuntime().availableProcessors()
        return (0 until cores).map { core ->
            try {
                val path = "/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq"
                File(path).readText().trim().toInt() / 1000
            } catch (e: Exception) {
                if (debugLogging) Log.w("DeviceInfo", "Failed to read CPU freq for core $core", e)
                0
            }
        }
    }

    private fun getGpuInfo(): GpuInfo {
        val (vendor, renderer, version, extensions) = HeadlessGpuInfoFetcher.fetch()
        return GpuInfo(
            vulkanVersion = getVulkanVersion(),
            glesVersion = version,
            vendor = vendor,
            renderer = renderer,
            extensions = extensions
        )
    }

    private fun getVulkanVersion(): String {
        val vulkan = getApplication<Application>().packageManager.systemAvailableFeatures
            .find { it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION }?.version ?: 0
        if (vulkan == 0) return ""
        return "${vulkan shr 22}.${vulkan shl 10 shr 22}.${vulkan shl 20 shr 22}"
    }

    private fun getRamInfo(): RamInfo {
        val memInfo = ActivityManager.MemoryInfo().apply {
            val am = getApplication<Application>().getSystemService(ActivityManager::class.java)
            am.getMemoryInfo(this)
        }
        val total = memInfo.totalMem / (1024f * 1024)
        val avail = memInfo.availMem / (1024f * 1024)
        return RamInfo(
            totalMB = total,
            availableMB = avail,
            availablePercent = (avail / total) * 100f,
            thresholdMB = memInfo.threshold / (1024f * 1024)
        )
    }

    fun getStorageInfo(context: Context): List<StorageInfo> {
        val storages = mutableListOf<StorageInfo>()

        try {
            val internal = Environment.getDataDirectory()
            val stat = StatFs(internal.path)
            storages.add(StorageInfo("Internal", stat.totalBytes, stat.totalBytes - stat.availableBytes, internal.path))
        } catch (e: Exception) {
            if (debugLogging) Log.w("Storage", "Failed to get internal storage", e)
        }

        @Suppress("DEPRECATION")
        ContextCompat.getExternalFilesDirs(context, null).forEachIndexed { index, file ->
            if (file != null) {
                val label = when {
                    index == 0 && !Environment.isExternalStorageRemovable(file) -> "External (Emulated)"
                    Environment.isExternalStorageRemovable(file) -> "External (Removable)"
                    else -> "Secondary External"
                }
                try {
                    val stat = StatFs(file.path)
                    if (storages.none { it.path == file.path }) {
                        storages.add(StorageInfo(label, stat.totalBytes, stat.totalBytes - stat.availableBytes, file.path))
                    }
                } catch (e: Exception) {
                    if (debugLogging) Log.w("Storage", "Failed to get external storage for $file", e)
                }
            }
        }

        getExtraSdCardMounts().forEach { path ->
            try {
                val stat = StatFs(path)
                if (storages.none { it.path == path }) {
                    storages.add(StorageInfo("External (Removable)", stat.totalBytes, stat.totalBytes - stat.availableBytes, path))
                }
            } catch (e: Exception) {
                if (debugLogging) Log.w("Storage", "Failed to get SD card mount for $path", e)
            }
        }

        return storages
    }

    private fun getExtraSdCardMounts(): List<String> {
        val result = mutableListOf<String>()
        try {
            val reader = BufferedReader(InputStreamReader(DataInputStream(FileInputStream("/proc/mounts"))))
            reader.lineSequence().forEach { line ->
                if (listOf("asec", "legacy", "Android/obb").any { line.contains(it) }) return@forEach
                if (listOf("/dev/block/vold/", "/dev/block/sd", "/dev/fuse", "/mnt/media_rw").any { line.startsWith(it) }) {
                    val path = line.split(" ").getOrNull(1) ?: return@forEach
                    val file = File(path)
                    if (file.exists() && !path.contains("/system") && path != Environment.getExternalStorageDirectory().path) {
                        result.add(path)
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            if (debugLogging) Log.w("Storage", "Failed to read /proc/mounts", e)
        }
        return result
    }

    private fun hasFeature(name: String): Boolean =
        getApplication<Application>().packageManager.hasSystemFeature(name)

    fun getScreenInfo(): ScreenInfo {
        val context = getApplication<Application>()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION") val display = windowManager.defaultDisplay
        val visibleMetrics = context.resources.displayMetrics
        @Suppress("DEPRECATION") val realMetrics = DisplayMetrics().apply { display.getRealMetrics(this) }
        val config = context.resources.configuration

        val screenClass = when (config.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
            Configuration.SCREENLAYOUT_SIZE_SMALL -> "Small"
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> "Normal"
            Configuration.SCREENLAYOUT_SIZE_LARGE -> "Large"
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> "XLarge"
            else -> "Undefined"
        }

        val densityClass = when (visibleMetrics.densityDpi) {
            in 0..160 -> "mdpi"
            in 161..240 -> "hdpi"
            in 241..320 -> "xhdpi"
            in 321..480 -> "xxhdpi"
            else -> "xxxhdpi"
        }

        return ScreenInfo(
            screenClass = screenClass,
            densityClass = densityClass,
            widthPx = visibleMetrics.widthPixels,
            heightPx = visibleMetrics.heightPixels,
            dpWidth = (visibleMetrics.widthPixels / visibleMetrics.density).toInt(),
            dpHeight = (visibleMetrics.heightPixels / visibleMetrics.density).toInt(),
            density = visibleMetrics.density,
            absoluteWidthPx = realMetrics.widthPixels,
            absoluteHeightPx = realMetrics.heightPixels,
            refreshRateHz = display.refreshRate,
            orientation = display.rotation,
            absoluteDpWidth = (realMetrics.widthPixels / visibleMetrics.density).toInt(),
            absoluteDpHeight = (realMetrics.heightPixels / visibleMetrics.density).toInt()
        )
    }

    @SuppressLint("HardwareIds")
    private fun getOsInfo(): OsInfo {
        val context = getApplication<Application>()
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        @Suppress("DEPRECATION") val encryptionStatus = try {
            when (dpm.storageEncryptionStatus) {
                DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> "UNSUPPORTED"
                DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> "INACTIVE"
                DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING -> "ACTIVATING"
                DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> "ACTIVE"
                DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER -> "ACTIVE_PER_USER"
                else -> "UNKNOWN"
            }
        } catch (_: Exception) {
            "UNKNOWN"
        }

        val getProp = { name: String -> getSystemProperty(name) }
        val miuiVersion = if (Build.MANUFACTURER.equals("Xiaomi", true)) getProp("ro.miui.ui.version.name") else null
        val osName = when {
            getProp("ro.build.version.harmonyos") != null -> "HarmonyOS"
            getProp("ro.build.version.emui") != null -> "EMUI"
            getProp("ro.build.version.opporom") != null -> "ColorOS"
            getProp("ro.build.display.id")?.contains("Flyme", true) == true -> "FlymeOS"
            getProp("ro.fireos.version") != null -> "FireOS"
            else -> "Android"
        }

        return OsInfo(
            osName = osName,
            version = Build.VERSION.RELEASE ?: "Unknown",
            sdk = Build.VERSION.SDK_INT,
            codename = Build.VERSION.CODENAME,
            bootloader = getBootloaderProperty().orEmpty(),
            brand = Build.BRAND,
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            board = Build.BOARD,
            vm = getVmInfo(),
            kernel = System.getProperty("os.version") ?: "Unknown",
            serial = "Unknown",
            language = Locale.getDefault().language,
            androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            isRooted = checkIsRooted(),
            encryptedStorage = encryptionStatus,
            strongBox = if (hasFeature("android.hardware.strongbox_keystore")) "Yes" else "No",
            fingerprint = Build.FINGERPRINT ?: "Unknown",
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            isEmulator = isProbablyEmulator(),
            miuiVersion = miuiVersion,
            fcmToken = MobeetestApp.fcm_token,
            fcmUuid = MobeetestApp.fcm_uuid,
            securityProviders = Security.getProviders().associate { it.name to it.version.toString() }
        )
    }

    private fun getVmInfo(): String {
        val name = System.getProperty("java.vm.name")?.trim().orEmpty()
        val version = System.getProperty("java.vm.version")?.trim().orEmpty()
        return when {
            name.contains("ART", true) -> "ART"
            version.startsWith("2") || name.contains("2") -> "ART"
            else -> name.ifEmpty { "Unknown" }
        }
    }

    private fun getBootloaderProperty(): String? = try {
        Runtime.getRuntime().exec("getprop ro.bootloader").inputStream.bufferedReader().readLine()?.takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }

    @SuppressLint("PrivateApi")
    private fun getSystemProperty(propName: String): String? = try {
        val clazz = Class.forName("android.os.SystemProperties")
        val getter = clazz.getMethod("get", String::class.java)
        (getter.invoke(null, propName) as? String)?.takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }

    private fun isProbablyEmulator(): Boolean = Build.FINGERPRINT.contains("generic") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            Build.HARDWARE in listOf("goldfish", "ranchu") ||
            Build.BOARD == "QC_Reference_Phone"

    private fun checkIsRooted(): Boolean = arrayOf(
        "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su"
    ).any { File(it).exists() }

    private fun getHardwareInfo(): HardwareInfo {
        val context = getApplication<Application>()
        val battery = getBatteryInfo(context)
        val cameras = getCameraInfo(context)
        val wireless = getWirelessInfo(context)
        val usb = UsbInfo(otg = hasFeature(PackageManager.FEATURE_USB_HOST))

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val soundCardCount = (audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).size +
                audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).size).coerceAtLeast(1)

        return HardwareInfo(battery, cameras, soundCardCount, wireless, usb)
    }

    private suspend fun getWeatherInfo(context:Context):String{
        // Try to get a best-effort location (GPS / Network / fused last-known)
        val location = BestLocationProvider.getBestAvailableLocation(context)
            ?: // No location → just return device info without weather
            return ""

        return getWeatherDetails(location.latitude, location.longitude)
    }

    private fun getBatteryInfo(context: Context): BatteryInfo = try {
        // Sticky broadcast with latest battery state
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val capacityMah = getBatteryCapacity(context)
            .toFloat()
            .coerceAtLeast(0f)

        if (intent == null) {
            // Could not read battery broadcast, return minimal info
            BatteryInfo(
                levelPercent = null,
                health = null,
                chargerConnection = null,
                status = null,
                temperatureC = null,
                capacityMah = capacityMah,
                technology = "Unknown"
            )
        } else {
            // Level %
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val levelPercent = if (level >= 0 && scale > 0) {
                (level * 100) / scale
            } else {
                null
            }

            // Raw values from system
            val healthRaw = intent.getIntExtra(
                BatteryManager.EXTRA_HEALTH,
                BatteryManager.BATTERY_HEALTH_UNKNOWN
            )
            val pluggedRaw = intent.getIntExtra(
                BatteryManager.EXTRA_PLUGGED,
                0
            )
            val statusRaw = intent.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            )

            // Temperature (in tenths of °C)
            val tempTenth = intent.getIntExtra(
                BatteryManager.EXTRA_TEMPERATURE,
                Int.MIN_VALUE
            )
            val temperatureC = if (tempTenth != Int.MIN_VALUE) {
                tempTenth / 10f
            } else {
                null
            }

            // Technology string
            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
                ?: "Unknown"

            val health = mapBatteryHealth(healthRaw)
            val chargerConnection = mapChargerConnection(pluggedRaw)
            val status = mapBatteryStatus(statusRaw)

            if (debugLogging) {
                Log.d(
                    "DeviceInfo",
                    "Battery snapshot: level=$levelPercent%, health=$health, " +
                            "charger=$chargerConnection, status=$status, " +
                            "temp=$temperatureC°C, capacity=${capacityMah}mAh, tech=$technology"
                )
            }

            BatteryInfo(
                levelPercent = levelPercent,
                health = health,
                chargerConnection = chargerConnection,
                status = status,
                temperatureC = temperatureC,
                capacityMah = capacityMah,
                technology = technology
            )
        }
    } catch (e: Exception) {
        if (debugLogging) Log.w("DeviceInfo", "Failed to read battery info", e)
        BatteryInfo(
            levelPercent = null,
            health = null,
            chargerConnection = null,
            status = null,
            temperatureC = null,
            capacityMah = 0f,
            technology = "Unknown"
        )
    }

    private fun mapBatteryHealth(raw: Int): BatteryHealth =
        when (raw) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVERVOLTAGE
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEATED
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealth.FAILED
            // BATTERY_HEALTH_COLD and any others
            else -> BatteryHealth.UNKNOWN
        }

    private fun mapChargerConnection(raw: Int): ChargerConnection =
        when {
            raw and BatteryManager.BATTERY_PLUGGED_AC != 0 -> ChargerConnection.AC
            raw and BatteryManager.BATTERY_PLUGGED_USB != 0 -> ChargerConnection.USB
            raw and BatteryManager.BATTERY_PLUGGED_WIRELESS != 0 -> ChargerConnection.WIRELESS
            raw == 0 -> ChargerConnection.NONE
            else -> ChargerConnection.UNKNOWN
        }

    private fun mapBatteryStatus(raw: Int): BatteryStatus =
        when (raw) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryStatus.DISCHARGING
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
            else -> BatteryStatus.UNKNOWN
        }


    @SuppressLint("PrivateApi")
    private fun getBatteryCapacity(context: Context): Double = try {
        val profile = Class.forName("com.android.internal.os.PowerProfile")
            .getConstructor(Context::class.java).newInstance(context)
        Class.forName("com.android.internal.os.PowerProfile")
            .getMethod("getAveragePower", String::class.java)
            .invoke(profile, "battery.capacity") as Double
    } catch (_: Exception) {
        -1.0
    }

    private fun getCameraInfo(context: Context): List<CameraInfo> = try {
        val cameraManager = context.getSystemService(CameraManager::class.java)
        cameraManager.cameraIdList.mapNotNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            val type = when (facing) {
                CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                CameraCharacteristics.LENS_FACING_BACK -> "Back"
                CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                else -> "Unknown"
            }
            CameraInfo(type, orientation)
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun getWirelessInfo(context: Context): WirelessInfo {
        val pm = context.packageManager
        val irManager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        val hasIr = irManager?.hasIrEmitter() == true

        return WirelessInfo(
            bluetooth = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            bluetoothLE = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE),
            gps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS),
            nfc = pm.hasSystemFeature(PackageManager.FEATURE_NFC),
            nfcCardEmulation = pm.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION),
            wifi = pm.hasSystemFeature(PackageManager.FEATURE_WIFI),
            wifiAware = pm.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE),
            wifiDirect = pm.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT),
            wifiPasspoint = pm.hasSystemFeature(PackageManager.FEATURE_WIFI_PASSPOINT),
            wifi5Ghz = wifiManager.is5GHzBandSupported,
            wifiP2p = wifiManager.isP2pSupported,
            irEmitter = hasIr
        )
    }


    suspend fun getWeatherDetails(lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            var attempts = 0
            var backoff = 500L
            var json = ""
            while (attempts < 3 && json.isEmpty()) {
                try {
                    val url = "https://api.weatherapi.com/v1/current.json?key=61a3bc2b8ec84dd7b5f65505251903&q=$lat,$lon"
                    json = URL(url).readText()
                } catch (_: Exception) {
                    delay(backoff)
                    backoff *= 2
                }
                attempts++
            }
            json
        }
    }


    fun loadDeviceInfo() {
        viewModelScope.launch {
            _updateInProgress.value = true

            val context = getApplication<Application>().applicationContext

            // Αν θες, βάλε Dispatchers για βαριές δουλειές
            val base = withContext(Dispatchers.Default) {
                collectDeviceInfo()
            }

            val weather = withContext(Dispatchers.IO) {
                getWeatherInfo(context)
            }

            val cpu = withContext(Dispatchers.Default) {
                cpuMonitor.getAppCpuUsageOneShot()
            }

            _deviceInfo.value = base.copy(
                weatherInfo = weather,
                mobeetestInfo = base.mobeetestInfo.copy(
                    cpuPercentageUsage = cpu
                )
            )

            _updateInProgress.value = false
        }
    }

}






object BestLocationProvider {

    // Target accuracy: 20 meters (or better)
    private const val DESIRED_ACCURACY_METERS = 20f

    // How old a last-known location is still considered useful (e.g. metro scenario)
    private const val MAX_LAST_KNOWN_AGE_MS = 30 * 60 * 1000L // 30 minutes

    // Timeout for getCurrentLocation to avoid hanging
    private const val CURRENT_LOCATION_TIMEOUT_MS = 7_000L

    /**
     * One-shot best location:
     * 1. Use the best recent last-known location (GPS / Network / Passive / fused)
     * 2. If not good enough, try getCurrentLocation() from Network provider
     * 3. If still not good enough, try getCurrentLocation() from GPS provider
     * 4. As a final fallback, return any last-known we have.
     */
    suspend fun getBestAvailableLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        val locationManager = ContextCompat.getSystemService(context, LocationManager::class.java)
            ?: return null

        // 1) Fast path: best last-known (good for metro/indoor)
        val bestLastKnown = getBestLastKnown(locationManager)
        if (bestLastKnown != null && isGoodEnough(bestLastKnown)) {
            return bestLastKnown
        }

        // 2) Try network one-shot (usually faster than GPS)
        val networkLocation = tryGetCurrentLocation(
            context = context,
            locationManager = locationManager,
            provider = LocationManager.NETWORK_PROVIDER
        )
        if (networkLocation != null && isGoodEnough(networkLocation)) {
            return networkLocation
        }

        // 3) Try GPS one-shot (more accurate, may be slower)
        val gpsLocation = tryGetCurrentLocation(
            context = context,
            locationManager = locationManager,
            provider = LocationManager.GPS_PROVIDER
        )
        if (gpsLocation != null && isGoodEnough(gpsLocation)) {
            return gpsLocation
        }

        // 4) As a last resort, return whatever last-known we had (even if not ideal)
        return bestLastKnown ?: getBestLastKnown(locationManager)
    }

    private fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Chooses the best last-known from all relevant providers.
     * This is very fast and helps in "metro" or indoor scenarios.
     */
    @SuppressLint("MissingPermission")
    private fun getBestLastKnown(locationManager: LocationManager): Location? {
        val providers = buildList {
            add(LocationManager.GPS_PROVIDER)
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)

            // Optional fused provider if the device exposes it by name
            if (locationManager.allProviders.contains("fused")) {
                add("fused")
            }
        }.distinct()

        var best: Location? = null

        for (provider in providers) {
            val loc = runCatching {
                locationManager.getLastKnownLocation(provider)
            }.getOrNull() ?: continue

            if (best == null) {
                best = loc
                continue
            }

            val isNewer = loc.time > best.time
            val isMoreAccurate = loc.hasAccuracy() && best.hasAccuracy() &&
                    loc.accuracy < best.accuracy

            val replace = when {
                isMoreAccurate && isNewer -> true
                isMoreAccurate -> true
                isNewer && best.accuracy > DESIRED_ACCURACY_METERS -> true
                else -> false
            }

            if (replace) {
                best = loc
            }
        }

        return best
    }

    /**
     * Checks if a location is fresh enough and accurate enough for our weather use-case.
     */
    private fun isGoodEnough(location: Location): Boolean {
        val now = System.currentTimeMillis()
        val age = now - location.time
        val accuracy = if (location.hasAccuracy()) location.accuracy else Float.MAX_VALUE

        return accuracy <= DESIRED_ACCURACY_METERS && age <= MAX_LAST_KNOWN_AGE_MS
    }

    /**
     * One-shot getCurrentLocation() wrapper with timeout.
     * Uses the given provider (Network or GPS).
     */
    @SuppressLint("MissingPermission")
    private suspend fun tryGetCurrentLocation(
        context: Context,
        locationManager: LocationManager,
        provider: String,
        timeoutMillis: Long = CURRENT_LOCATION_TIMEOUT_MS
    ): Location? = suspendCancellableCoroutine { cont ->
        // If provider is disabled, do not even try
        if (!locationManager.isProviderEnabled(provider)) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        val mainHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (cont.isActive) {
                cont.resume(null)
            }
        }

        mainHandler.postDelayed(timeoutRunnable, timeoutMillis)
        val executor = ContextCompat.getMainExecutor(context)

        try {
            locationManager.getCurrentLocation(
                provider,
                null,
                executor
            ) { location ->
                mainHandler.removeCallbacks(timeoutRunnable)
                if (cont.isActive) {
                    cont.resume(location)
                }
            }
        } catch (_: SecurityException) {
            mainHandler.removeCallbacks(timeoutRunnable)
            if (cont.isActive) {
                cont.resume(null)
            }
        }

        cont.invokeOnCancellation {
            mainHandler.removeCallbacks(timeoutRunnable)
        }
    }
}

class CpuMonitor {

    private fun readAppCpuTicks(): Long? {
        val pid = Process.myPid()
        val statFile = File("/proc/$pid/stat")
        if (!statFile.exists()) return null

        val parts = statFile.readText().split(" ")
        val utime = parts.getOrNull(13)?.toLongOrNull() ?: return null
        val stime = parts.getOrNull(14)?.toLongOrNull() ?: return null
        return utime + stime
    }

    private fun clockTicksPerSecond(): Long {
        return try {
            Os.sysconf(OsConstants._SC_CLK_TCK)
        } catch (_: Throwable) {
            100L // safe fallback
        }
    }

    /**
     * One-shot CPU usage for the app.
     * Measures two samples within a short window.
     */
    suspend fun getAppCpuUsageOneShot(sampleWindowMs: Long = 250L): Float? =
        withContext(Dispatchers.IO) {

            val t1ticks = readAppCpuTicks() ?: return@withContext null
            val t1 = SystemClock.elapsedRealtime()

            delay(sampleWindowMs)

            val t2ticks = readAppCpuTicks() ?: return@withContext null
            val t2 = SystemClock.elapsedRealtime()

            val deltaTicks = (t2ticks - t1ticks).coerceAtLeast(0)
            val deltaMs = (t2 - t1).coerceAtLeast(1)

            val ticksPerSec = clockTicksPerSecond()
            val cpuDeltaMs = deltaTicks * 1000f / ticksPerSec

            // This is roughly "percent of one core".
            val usage = (cpuDeltaMs / deltaMs) * 100f

            usage.coerceIn(0f, 100f)
        }
}