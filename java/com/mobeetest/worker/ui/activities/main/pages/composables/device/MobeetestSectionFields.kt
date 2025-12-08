package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.runtime.Composable
import com.mobeetest.worker.data.model.device.MobeetestInfo

@Composable
fun MobeetestSectionFields(mobeetest: MobeetestInfo, iconRes: Int) {
    val rows = buildMobeetestRows(mobeetest)

    rows.forEachIndexed { index, row ->
        when (row.rowType) {
            MobeetestRowType.VALUE -> {
                DeviceInfoValueRow(
                    index = index + 1,
                    iconRes = iconRes,
                    label = row.label,
                    value = row.value,
                    infoDescription = row.infoDescription,
                    showBottomDivider = (index < rows.lastIndex)
                )
            }
            MobeetestRowType.DATE -> {
                val millis = row.dateMillis ?: System.currentTimeMillis()
                DeviceInfoDateFieldRow(
                    index = index + 1,
                    iconRes = iconRes,
                    label = row.label,
                    millis = millis,
                    valueOverride = row.value,
                    infoDescription = row.infoDescription,
                    showBottomDivider = (index < rows.lastIndex)
                )
            }
            MobeetestRowType.DATE_TIME -> {
                val millis = row.dateMillis ?: System.currentTimeMillis()
                DeviceInfoDateTimeFieldRow(
                    index = index + 1,
                    iconRes = iconRes,
                    label = row.label,
                    millis = millis,
                    valueOverride = row.value,
                    infoDescription = row.infoDescription,
                    showBottomDivider = (index < rows.lastIndex)
                )
            }
            MobeetestRowType.BOOLEAN -> {
                DeviceInfoBooleanFieldRow(
                    index = index + 1,
                    iconRes = iconRes,
                    label = row.label,
                    value = row.boolValue ?: false,
                    infoDescription = row.infoDescription,
                    showBottomDivider = (index < rows.lastIndex)
                )
            }
        }
    }
}
