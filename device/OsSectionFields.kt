package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.device.OsInfo

@Composable
fun OsSectionFields(os: OsInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_os_name),
        value = os.osName,
        infoDescription = "Name of the operating system reported by the device (for example Android)."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_version),
        value = os.version,
        infoDescription = "Human-readable OS version string shown to the user."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_sdk_level),
        value = os.sdk.toString(),
        infoDescription = "Android API level (SDK_INT) used by apps to check feature availability."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_codename),
        value = os.codename,
        infoDescription = "Internal Android codename associated with this release."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_manufacturer),
        value = os.manufacturer,
        infoDescription = "Device manufacturer as reported by Build.MANUFACTURER."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_brand),
        value = os.brand,
        infoDescription = "High-level brand name (for example Google, Samsung, Xiaomi)."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_model),
        value = os.model,
        infoDescription = "Marketing model string shown to users and apps."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_board),
        value = os.board,
        infoDescription = "Low-level board or hardware platform identifier."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_kernel),
        value = os.kernel,
        infoDescription = "Linux kernel version and build information."
    )

    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_rooted),
        value = os.isRooted,
        infoDescription = "Best-effort check indicating whether the device appears to be rooted."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_running_on_emulator),
        value = os.isEmulator,
        infoDescription = "Best-effort check indicating whether this device seems to be an emulator."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_encrypted_storage),
        value = os.encryptedStorage,
        infoDescription = "Describes whether the primary storage is reported as encrypted."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_strongbox),
        value = os.strongBox,
        infoDescription = "Indicates presence of a StrongBox-backed hardware security module when available."
    )

    os.miuiVersion?.takeIf { it.isNotBlank() }?.let {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_miui_version),
            value = it,
            infoDescription = "MIUI firmware version string for Xiaomi devices."
        )
    }

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_language),
        value = os.language,
        infoDescription = "Current primary system language (ISO code)."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_android_id),
        value = os.androidId,
        infoDescription = "Stable Android ID string scoped to this device and user profile."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_fingerprint),
        value = os.fingerprint,
        infoDescription = "Build fingerprint uniquely identifying this OS build and device configuration."
    )

    if (os.supportedAbis.isNotEmpty()) {
        DeviceInfoTextListField(
            index = index++,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_supported_abis),
            values = os.supportedAbis,
            infoDescription = "Ordered list of all native ABIs supported by the runtime on this device."
        )
    }

    if (os.securityProviders.isNotEmpty()) {
        DeviceInfoKeyValueListField(
            index = index++,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_security_providers),
            pairs = os.securityProviders,
            infoDescription = "Installed Java security providers and their versions, as seen by the platform.",
            showBottomDivider = (os.fcmToken != null && os.fcmUuid != null)
        )
    }

    os.fcmToken?.takeIf { it.isNotBlank() }?.let { token ->
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_fcm_token),
            value = token,
            infoDescription = "Current Firebase Cloud Messaging registration token used to reach this device.",
            showBottomDivider = (os.fcmUuid != null)
        )
    }

    os.fcmUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_fcm_uuid),
            value = uuid,
            infoDescription = "Internal UUID used by the app to associate the device with backend records.",
            showBottomDivider = false
        )
    }
}
