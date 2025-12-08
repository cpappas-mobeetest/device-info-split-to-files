package com.mobeetest.worker.ui.activities.main.pages.composables.device

internal enum class MobeetestRowType { VALUE, DATE, DATE_TIME, BOOLEAN }

internal data class MobeetestRowSpec(
    val label: String,
    val value: String,
    val infoDescription: String? = null,
    val rowType: MobeetestRowType = MobeetestRowType.VALUE,
    val dateMillis: Long? = null,
    val boolValue: Boolean? = null
)
