package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.device.CpuInfo

@Composable
fun CpuSectionFields(cpu: CpuInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_soc_name),
        value = cpu.socName,
        infoDescription = "Human-readable name of the system-on-chip (CPU family) reported by the device."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_primary_abi),
        value = cpu.abi,
        infoDescription = "Main native architecture that apps should target on this device (for example arm64-v8a or x86_64)."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_cores),
        value = cpu.numberOfCores.toString(),
        infoDescription = "Total number of CPU cores exposed to Android."
    )

    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_arm_neon_support),
        value = cpu.armNeon,
        infoDescription = "Indicates whether the CPU supports the ARM NEON SIMD instruction set for accelerated math and media workloads."
    )

    DeviceInfoTextListField(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_max_frequencies),
        values = cpu.maxFrequenciesMHz.mapIndexed { coreIndex, mhz ->
            if (mhz > 0) "Core $coreIndex: $mhz MHz" else "Core $coreIndex: Unknown"
        },
        infoDescription = "Maximum configured clock frequency for each CPU core, in megahertz."
    )

    DeviceInfoTextListField(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_l1d_cache),
        values = cpu.l1dCache,
        infoDescription = "Per-core level 1 data cache sizes reported by the system."
    )
    DeviceInfoTextListField(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_l1i_cache),
        values = cpu.l1iCache,
        infoDescription = "Per-core level 1 instruction cache sizes reported by the system."
    )
    DeviceInfoTextListField(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_l2_cache),
        values = cpu.l2Cache,
        infoDescription = "Level 2 cache sizes, usually shared by a cluster of CPU cores."
    )
    DeviceInfoTextListField(
        index = index,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_l3_cache),
        values = cpu.l3Cache,
        infoDescription = "Last-level (L3) cache sizes, typically shared across multiple CPU cores.",
        showBottomDivider = false
    )
}
