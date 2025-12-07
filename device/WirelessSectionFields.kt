package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.runtime.Composable
import com.mobeetest.worker.data.model.device.WirelessInfo

@Composable
fun WirelessSectionFields(w: WirelessInfo, iconRes: Int) {
    var index = 1

    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Bluetooth",
        value = w.bluetooth,
        infoDescription = "Indicates whether the device reports support for classic Bluetooth.",
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Bluetooth LE",
        value = w.bluetoothLE,
        infoDescription = "Indicates whether the device supports Bluetooth Low Energy (BLE)."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "GPS",
        value = w.gps,
        infoDescription = "Indicates whether the device has a GPS location provider."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "NFC",
        value = w.nfc,
        infoDescription = "Indicates whether the device supports Near Field Communication."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "NFC card emulation",
        value = w.nfcCardEmulation,
        infoDescription = "Whether the NFC hardware can emulate cards for secure transactions or access control."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi",
        value = w.wifi,
        infoDescription = "Indicates whether the device has Wi-Fi networking capability."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi Aware",
        value = w.wifiAware,
        infoDescription = "Support for Wi-Fi Aware (neighbor awareness networking) APIs."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi Direct",
        value = w.wifiDirect,
        infoDescription = "Support for Wi-Fi Direct peer-to-peer connections."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi Passpoint",
        value = w.wifiPasspoint,
        infoDescription = "Support for Wi-Fi Passpoint / Hotspot 2.0 automatic hotspot roaming."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi 5 GHz band",
        value = w.wifi5Ghz,
        infoDescription = "Whether the Wi-Fi radio can use the 5 GHz band."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi P2P",
        value = w.wifiP2p,
        infoDescription = "Whether the device reports support for Wi-Fi peer-to-peer APIs."
    )
    DeviceInfoBooleanFieldRow(
        index = index,
        iconRes = iconRes,
        label = "IR emitter",
        value = w.irEmitter,
        infoDescription = "Indicates if the device has an infrared (IR) emitter for remote-control use cases.",
        showBottomDivider = false
    )
}
