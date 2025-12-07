package com.mobeetest.worker.ui.activities.main.pages.composables.device

internal enum class MobeetestRowType { 
    VALUE,      // Plain text value row
    DATE,       // Date only (with calendar visual)
    DATE_TIME,  // Date and time (with clock visual)
    BOOLEAN     // Boolean value (Yes/No)
}

internal data class MobeetestRowSpec(
    val label: String,
    val value: String,
    val infoDescription: String? = null,
    val rowType: MobeetestRowType = MobeetestRowType.VALUE,
    val dateMillis: Long? = null,
    val boolValue: Boolean? = null
)
