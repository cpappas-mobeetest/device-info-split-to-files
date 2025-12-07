package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R

@Composable
fun BottomLogos(manufactor: String) {
    val normalized = manufactor.trim().lowercase()

    val manufactorResource = when (normalized) {
        "", "unknown", "generic", "android", "aosp" -> R.drawable.oem_generic
        "samsung" -> R.drawable.oem_samsung
        "google" -> R.drawable.oem_google
        "lg", "lge" -> R.drawable.oem_lg
        "huawei" -> R.drawable.oem_huawei
        "honor" -> R.drawable.oem_honor
        "xiaomi" -> R.drawable.oem_xiaomi
        "redmi" -> R.drawable.oem_redmi
        "poco" -> R.drawable.oem_poco
        "oppo" -> R.drawable.oem_oppo
        "oneplus" -> R.drawable.oem_oneplus
        "realme" -> R.drawable.oem_realme
        "vivo" -> R.drawable.oem_vivo
        "iqoo" -> R.drawable.oem_iqoo
        "motorola" -> R.drawable.oem_motorola
        "lenovo" -> R.drawable.oem_lenovo
        "sony" -> R.drawable.oem_sony
        "asus" -> R.drawable.oem_asus
        "nokia", "hmd global", "hmd global oy" -> R.drawable.oem_nokia
        "zte" -> R.drawable.oem_zte
        "tcl" -> R.drawable.oem_tcl
        "alcatel" -> R.drawable.oem_alcatel
        "tecno" -> R.drawable.oem_tecno
        "infinix" -> R.drawable.oem_infinix
        "itel" -> R.drawable.oem_itel
        "nothing" -> R.drawable.oem_nothing
        "amazon" -> R.drawable.oem_amazon
        else -> R.drawable.oem_generic
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = manufactorResource),
            contentDescription = "Manufactor logo",
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Mobeetest logo",
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )
        Icon(
            painter = painterResource(id = R.drawable.android),
            contentDescription = "Android logo",
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )
    }
}
