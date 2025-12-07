package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.device.DeviceInfo

internal fun buildDeviceInfoSections(): List<DeviceInfoItem> {
    return listOf(
        DeviceInfoItem(
            id = "cpu",
            title = "CPU",
            description = "Information about the main processor (SoC): number of cores, maximum frequencies, supported ABIs and cache levels. This section will show the processing capabilities of the device.",
            iconRes = R.drawable.cpu
        ),
        DeviceInfoItem(
            id = "gpu",
            title = "GPU",
            description = "Information about the the graphics processor: Vulkan / OpenGL ES versions, vendor, renderer and supported extensions. Useful for evaluating graphics capabilities and API support.",
            iconRes = R.drawable.gpu
        ),
        DeviceInfoItem(
            id = "ram",
            title = "RAM",
            description = "Total memory, available memory and usage percentage. Helps you understand how loaded the device is while mobeetest is running.",
            iconRes = R.drawable.ram
        ),
        DeviceInfoItem(
            id = "storage",
            title = "Storage",
            description = "List of all storage volumes (internal, external, SD): total space, used space and file system paths. This shows where mobeetest files are stored on the device.",
            iconRes = R.drawable.storage
        ),
        DeviceInfoItem(
            id = "screen",
            title = "Screen",
            description = "Display information: resolution in pixels, dp dimensions, density class (mdpi/hdpi/...), refresh rate and orientation. Very useful for debugging UI layouts and responsiveness.",
            iconRes = R.drawable.screen
        ),
        DeviceInfoItem(
            id = "os",
            title = "OS",
            description = "Details about the operating system: name (Android / HarmonyOS, etc.), version, SDK level, manufacturer, fingerprint, root status, language, Android ID and security providers.",
            iconRes = R.drawable.android
        ),
        DeviceInfoItem(
            id = "hardware",
            title = "Hardware",
            description = "Overview of the device hardware: battery, cameras, audio, wireless capabilities and USB/OTG. The following subcategories break this down by subsystem.",
            iconRes = R.drawable.hardware,
            children = listOf(
                DeviceInfoItem(
                    id = "hardware_battery",
                    title = "Battery",
                    description = "Battery capacity in mAh and battery technology (Li-ion, Li-Po, etc.). Useful to estimate how long the device can run continuous measurements.",
                    iconRes = R.drawable.battery
                ),
                DeviceInfoItem(
                    id = "hardware_cameras",
                    title = "Cameras",
                    description = "List of device cameras (front, back, external) with sensor orientation. Lets you verify which cameras are available and how they are positioned.",
                    iconRes = R.drawable.camera
                ),
                DeviceInfoItem(
                    id = "hardware_wireless",
                    title = "Wireless",
                    description = "Support for Bluetooth, Bluetooth LE, GPS, NFC, Wi-Fi (5 GHz, Direct, Passpoint), Wi-Fi Aware and IR emitter. Shows the wireless capabilities that can be used in measurement scenarios.",
                    iconRes = R.drawable.wireless
                ),
                DeviceInfoItem(
                    id = "hardware_usb",
                    title = "USB",
                    description = "Information about USB host / OTG support. Useful if you plan to connect external sensors or other devices in future mobeetest features.",
                    iconRes = R.drawable.usb
                ),
                DeviceInfoItem(
                    id = "hardware_sound_cards",
                    title = "Sound cards",
                    description = "Number of logical sound cards / audio output devices reported by the system.",
                    iconRes = R.drawable.sound_card
                )
            )
        ),
        DeviceInfoItem(
            id = "weather",
            title = "Weather",
            description = "Current outdoor weather conditions at the device location (city, temperature, humidity, wind, UV index and visibility). Icons are dynamically adjusted based on the values.",
            iconRes = R.drawable.weather_main
        ),
        DeviceInfoItem(
            id = "mobeetest",
            title = "Mobeetest",
            description = "Information about the Mobeetest application itself: edition, version, install / update timestamps, push identifiers and runtime statistics.",
            iconRes = R.drawable.ic_launcher_background
        )
    )
}

internal fun countFieldsForSection(
    itemId: String,
    deviceInfo: DeviceInfo?
): Int {
    if (deviceInfo == null) return 0

    return when (itemId) {
        "cpu" -> 9

        "gpu" -> {
            val gpu = deviceInfo.gpu
            var count = 2 // Vendor, Renderer
            if (gpu.glesVersion.isNotBlank()) count++
            if (gpu.vulkanVersion.isNotBlank()) count++
            count++ // Extensions list
            count
        }

        "ram" -> 4

        "storage" -> {
            val storages = deviceInfo.storage
            if (storages.isEmpty()) 1 else storages.size
        }

        "screen" -> 9

        "os" -> {
            val os = deviceInfo.os
            var count = 0

            // Base text fields
            count++ // OS name
            count++ // Version
            count++ // SDK level
            count++ // Codename
            count++ // Manufacturer
            count++ // Brand
            count++ // Model
            count++ // Board
            count++ // Kernel

            // Booleans
            count++ // Rooted
            count++ // Running on emulator

            // More text fields
            count++ // Encrypted storage
            count++ // StrongBox

            if (!os.miuiVersion.isNullOrBlank()) {
                count++ // MIUI version
            }

            count++ // Language
            count++ // Android ID
            count++ // Fingerprint

            if (os.supportedAbis.isNotEmpty()) count++
            if (os.securityProviders.isNotEmpty()) count++
            if (!os.fcmToken.isNullOrBlank()) count++
            if (!os.fcmUuid.isNullOrBlank()) count++

            count
        }

        "hardware_battery" -> 7

        "hardware_cameras" -> {
            val cams = deviceInfo.hardware.cameras
            if (cams.isEmpty()) 1 else 2
        }

        "hardware_wireless" -> 12

        "hardware_usb" -> 1
        "hardware_sound_cards" -> 1
        "weather" -> 11
        "mobeetest" -> {
            val m = deviceInfo.mobeetestInfo
            buildMobeetestRows(m).size +
                    (if (m.ramPercentageUsage != null) 1 else 0) +
                    (if (m.cpuPercentageUsage != null) 1 else 0)
        }
        else -> 0
    }
}
