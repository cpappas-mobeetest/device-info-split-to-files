package com.mobeetest.worker.activities.main.pages.composables.device

internal enum class MobeetestRowType { TEXT, DATE, DATETIME }

internal data class MobeetestRowSpec(
    val label: String,
    val value: String,
    val infoDescription: String? = null,
    val type: MobeetestRowType = MobeetestRowType.TEXT,
    val millis: Long? = null
)
