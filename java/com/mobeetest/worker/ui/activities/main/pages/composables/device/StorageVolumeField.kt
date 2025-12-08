package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.device.StorageInfo
import kotlinx.coroutines.delay

@Composable
fun StorageVolumeField(
    index: Int,
    iconRes: Int,
    storage: StorageInfo,
    showBottomDivider: Boolean = true
) {
    val clipboardManager = LocalClipboardManager.current
    
    var showInfo by rememberSaveable("storage_${index}_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable("storage_${index}_copy") { mutableStateOf(false) }

    val bgColor = deviceInfoFieldBackground(index)
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val headerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    val tableBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val rowEven = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
    val rowOdd = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)

    val usagePercent = if (storage.totalBytes > 0) {
        (storage.usedBytes.toDouble() / storage.totalBytes * 100.0).toFloat()
    } else 0f

    val rows = listOf(
        Triple(stringResource(R.string.device_info_label_storage_path), storage.path, null),
        Triple(stringResource(R.string.device_info_label_storage_total), formatBytes(storage.totalBytes), null),
        Triple(stringResource(R.string.device_info_label_storage_used), formatBytes(storage.usedBytes), usagePercent)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing12, top = deviceInfoSpacing6, bottom = deviceInfoSpacing6),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = deviceInfoFieldIndexTextStyle,
                modifier = Modifier.width(deviceInfoFieldIndexWidth)
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(deviceInfoIconSize29)
                    .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing4),
                tint = Color.Unspecified
            )

            Text(
                text = storage.label,
                style = deviceInfoFieldLabelTextStyle,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { showInfo = !showInfo },
                modifier = Modifier.size(deviceInfoIconSize24).padding(end = 2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.information),
                    contentDescription = stringResource(R.string.device_info_cd_info, storage.label),
                    tint = Color.Unspecified
                )
            }

            IconButton(
                onClick = {
                    val copyText = buildString {
                        appendLine("$index. ${storage.label}")
                        rows.forEachIndexed { i, (title, value, _) ->
                            appendLine("  ${index}.${i + 1}  $title: $value")
                        }
                    }.trim()
                    clipboardManager.setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier.size(deviceInfoIconSize24).padding(end = 2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = stringResource(R.string.device_info_cd_copy, storage.label),
                    tint = Color.Unspecified
                )
            }
        }

        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing12, bottom = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(R.string.device_info_copied_to_clipboard),
                    style = deviceInfoCopiedMessageTextStyle,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(visible = showInfo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = deviceInfoSpacing12, end = deviceInfoSpacing12, bottom = deviceInfoSpacing4)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(deviceInfoCornerRadius8)
                        )
                        .padding(horizontal = deviceInfoSpacing10, vertical = deviceInfoSpacing6)
                ) {
                    Text(
                        text = "Storage volume as reported by Android (path, total space, used space and usage percentage).",
                        style = deviceInfoInfoDescriptionTextStyle,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = deviceInfoSpacing16, vertical = deviceInfoSpacing4)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(deviceInfoCornerRadius8))
                .border(deviceInfoBorderThickness, outlineColor, androidx.compose.foundation.shape.RoundedCornerShape(deviceInfoCornerRadius8))
                .background(tableBg)
        ) {
            DynamicValueColumnTable(
                index = index,
                rows = rows,
                storage = storage,
                headerBg = headerBg,
                rowEven = rowEven,
                rowOdd = rowOdd,
                outlineColor = outlineColor
            )
        }

        if (showInfo) {
            LaunchedEffect(Unit) {
                delay(4_000)
                showInfo = false
            }
        }

        LaunchedEffect(showCopied) {
            if (showCopied) {
                delay(1_500)
                showCopied = false
            }
        }

        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = deviceInfoBorderThickness,
                color = deviceInfoDivider
            )
        }
    }
}
