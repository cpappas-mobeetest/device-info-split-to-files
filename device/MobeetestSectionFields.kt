package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mobeetest.worker.R
import com.mobeetest.worker.models.MobeetestInfo

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
                    value = row.value ?: stringResource(R.string.device_info_unknown),
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
