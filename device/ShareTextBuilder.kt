package com.mobeetest.worker.activities.main.pages.composables.device

import com.mobeetest.worker.data.model.device.DeviceInfo
import java.util.Locale

internal fun buildSectionShareText(
    item: DeviceInfoItem,
    deviceInfo: DeviceInfo?
): String {
    if (deviceInfo == null) {
        return item.title
    }

    return when (item.id) {
        "cpu" -> {
            val cpu = deviceInfo.cpu
            buildString {
                appendLine("CPU")
                appendLine("SoC name: ${cpu.socName}")
                appendLine("Primary ABI: ${cpu.abi}")
                appendLine("Cores: ${cpu.numberOfCores}")
                appendLine("ARM NEON support: ${boolYesNo(cpu.armNeon)}")

                if (cpu.maxFrequenciesMHz.isNotEmpty()) {
                    appendLine("Max frequencies (MHz):")
                    cpu.maxFrequenciesMHz.forEachIndexed { index, mhz ->
                        val text = if (mhz > 0) "$mhz MHz" else "Unknown"
                        appendLine("  • Core $index: $text")
                    }
                }

                if (cpu.l1dCache.isNotEmpty()) {
                    appendLine("L1d cache: ${cpu.l1dCache.joinToString()}")
                }
                if (cpu.l1iCache.isNotEmpty()) {
                    appendLine("L1i cache: ${cpu.l1iCache.joinToString()}")
                }
                if (cpu.l2Cache.isNotEmpty()) {
                    appendLine("L2 cache: ${cpu.l2Cache.joinToString()}")
                }
                if (cpu.l3Cache.isNotEmpty()) {
                    appendLine("L3 cache: ${cpu.l3Cache.joinToString()}")
                }
            }.trim()
        }

        "gpu" -> {
            val gpu = deviceInfo.gpu
            buildString {
                appendLine("GPU")
                appendLine("Vendor: ${gpu.vendor}")
                appendLine("Renderer: ${gpu.renderer}")
                if (gpu.glesVersion.isNotBlank()) {
                    appendLine("OpenGL ES: ${gpu.glesVersion}")
                }
                if (gpu.vulkanVersion.isNotBlank()) {
                    appendLine("Vulkan: ${gpu.vulkanVersion}")
                }
                if (gpu.extensions.isNotEmpty()) {
                    appendLine("Extensions:")
                    gpu.extensions.forEach { ext ->
                        appendLine("  • $ext")
                    }
                }
            }.trim()
        }

        "ram" -> {
            val ram = deviceInfo.ram
            buildString {
                appendLine("RAM")
                appendLine("Total: ${formatMegabytes(ram.totalMB)}")
                appendLine("Available: ${formatMegabytes(ram.availableMB)}")
                appendLine("Low memory threshold: ${formatMegabytes(ram.thresholdMB)}")
                appendLine("Available percent: ${ram.availablePercent.toInt()}%")
            }.trim()
        }

        "storage" -> {
            val storages = deviceInfo.storage
            buildString {
                appendLine("Storage")
                storages.forEach { s ->
                    appendLine()
                    appendLine("─ ${s.label}")
                    appendLine("Path: ${s.path}")
                    appendLine("Total: ${formatBytes(s.totalBytes)}")
                    appendLine("Used: ${formatBytes(s.usedBytes)}")
                    appendLine("Used space: ${s.percentUsed}%")
                }
            }.trim()
        }

        "screen" -> {
            val screen = deviceInfo.screen
            val orientationLabel = when (screen.orientation) {
                0 -> "0 (Portrait)"
                1 -> "90 (Landscape)"
                2 -> "180 (Reverse portrait)"
                3 -> "270 (Reverse landscape)"
                else -> screen.orientation.toString()
            }
            buildString {
                appendLine("Screen")
                appendLine("Screen class: ${screen.screenClass}")
                appendLine("Density class: ${screen.densityClass ?: "Unknown"}")
                appendLine("Visible size (px): ${screen.widthPx} x ${screen.heightPx}")
                appendLine("Visible size (dp): ${screen.dpWidth} x ${screen.dpHeight}")
                appendLine("Absolute size (px): ${screen.absoluteWidthPx} x ${screen.absoluteHeightPx}")
                appendLine("Absolute size (dp): ${screen.absoluteDpWidth} x ${screen.absoluteDpHeight}")
                appendLine("Density: ${String.format(Locale.US, "%.2f", screen.density)}")
                appendLine("Refresh rate: ${String.format(Locale.US, "%.1f Hz", screen.refreshRateHz)}")
                appendLine("Orientation: $orientationLabel")
            }.trim()
        }

        "os" -> {
            val os = deviceInfo.os
            buildString {
                appendLine("OS / Device")
                appendLine("OS name: ${os.osName}")
                appendLine("Version: ${os.version}")
                appendLine("SDK level: ${os.sdk}")
                appendLine("Codename: ${os.codename}")
                appendLine("Manufacturer: ${os.manufacturer}")
                appendLine("Brand: ${os.brand}")
                appendLine("Model: ${os.model}")
                appendLine("Board: ${os.board}")
                appendLine("Kernel: ${os.kernel}")
                appendLine("Rooted: ${boolYesNo(os.isRooted)}")
                appendLine("Running on emulator: ${boolYesNo(os.isEmulator)}")
                appendLine("Encrypted storage: ${os.encryptedStorage}")
                appendLine("StrongBox: ${os.strongBox}")
                os.miuiVersion?.takeIf { it.isNotBlank() }?.let {
                    appendLine("MIUI version: $it")
                }
                appendLine("Language: ${os.language}")
                appendLine("Android ID: ${os.androidId}")
                appendLine("Fingerprint: ${os.fingerprint}")

                if (os.supportedAbis.isNotEmpty()) {
                    appendLine("Supported ABIs:")
                    os.supportedAbis.forEach { abi ->
                        appendLine("  • $abi")
                    }
                }

                if (os.securityProviders.isNotEmpty()) {
                    appendLine("Security providers:")
                    os.securityProviders.forEach { (name, ver) ->
                        appendLine("  • $name: $ver")
                    }
                }

                os.fcmToken?.takeIf { it.isNotBlank() }?.let { token ->
                    appendLine("FCM token: $token")
                }
                os.fcmUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
                    appendLine("FCM UUID: $uuid")
                }
            }.trim()
        }

        "hardware_battery" -> {
            val b = deviceInfo.hardware.battery
            buildString {
                appendLine("Battery")

                val levelStr = b.levelPercent?.let { "$it %" } ?: "Unknown"
                appendLine("Level: $levelStr")

                appendLine("Status: ${describeBatteryStatus(b.status)}")
                appendLine("Power source: ${describeChargerConnection(b.chargerConnection)}")
                appendLine("Health: ${describeBatteryHealth(b.health)}")

                b.temperatureC?.let {
                    appendLine("Temperature: ${String.format(Locale.US, "%.1f °C", it)}")
                }

                val capacityStr = if (b.capacityMah > 0f) {
                    "${b.capacityMah.toInt()} mAh"
                } else {
                    "Unknown"
                }
                appendLine("Capacity: $capacityStr")
                appendLine("Technology: ${b.technology}")
            }.trim()
        }

        "hardware_sound_cards" -> {
            val b = deviceInfo.hardware.soundCardCount
            buildString {
                appendLine("Sound card")
                appendLine("Number of sound cards: $b")
            }.trim()
        }

        "hardware_cameras" -> {
            val cams = deviceInfo.hardware.cameras
            buildString {
                appendLine("Cameras")
                appendLine("Total: ${cams.size}")
                if (cams.isNotEmpty()) {
                    appendLine("Available cameras:")
                    cams.forEachIndexed { index, cam ->
                        appendLine("  • Camera $index: ${cam.type} (${cam.orientation}°)")
                    }
                }
            }.trim()
        }

        "hardware_wireless" -> {
            val w = deviceInfo.hardware.wireless
            buildString {
                appendLine("Wireless")
                appendLine("Bluetooth: ${boolYesNo(w.bluetooth)}")
                appendLine("Bluetooth LE: ${boolYesNo(w.bluetoothLE)}")
                appendLine("GPS: ${boolYesNo(w.gps)}")
                appendLine("NFC: ${boolYesNo(w.nfc)}")
                appendLine("NFC card emulation: ${boolYesNo(w.nfcCardEmulation)}")
                appendLine("Wi-Fi: ${boolYesNo(w.wifi)}")
                appendLine("Wi-Fi Aware: ${boolYesNo(w.wifiAware)}")
                appendLine("Wi-Fi Direct: ${boolYesNo(w.wifiDirect)}")
                appendLine("Wi-Fi Passpoint: ${boolYesNo(w.wifiPasspoint)}")
                appendLine("Wi-Fi 5 GHz band: ${boolYesNo(w.wifi5Ghz)}")
                appendLine("Wi-Fi P2P: ${boolYesNo(w.wifiP2p)}")
                appendLine("IR emitter: ${boolYesNo(w.irEmitter)}")
            }.trim()
        }

        "hardware_usb" -> {
            val usb = deviceInfo.hardware.usb
            buildString {
                appendLine("USB")
                appendLine("USB OTG / Host support: ${boolYesNo(usb.otg)}")
            }.trim()
        }

        "hardware" -> {
            buildString {
                appendLine("Hardware")
                appendLine(item.description)
            }.trim()
        }

        "weather" -> {
            val raw = deviceInfo.weatherInfo
            val map = raw?.let { formatWeatherInfo(it) }.orEmpty()

            buildString {
                appendLine("Weather")
                map.forEach { (k, v) ->
                    appendLine("$k: $v")
                }
            }.trim()
        }

        else -> item.title
    }
}
