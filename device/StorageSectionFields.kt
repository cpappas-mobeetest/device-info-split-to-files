package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.device.StorageInfo

@Composable
fun StorageSectionFields(storages: List<StorageInfo>, iconRes: Int) {
    if (storages.isEmpty()) {
        DeviceInfoValueRow(
            index = 1,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_storage_volumes),
            value = stringResource(R.string.device_info_none_detected),
            infoDescription = "No storage volumes (internal or external) were reported by the system."
        )
        return
    }

    storages.forEachIndexed { i, storage ->
        StorageVolumeField(
            index = i + 1,
            iconRes = iconRes,
            storage = storage,
            showBottomDivider = (i < storages.size - 1)
        )
    }
}
