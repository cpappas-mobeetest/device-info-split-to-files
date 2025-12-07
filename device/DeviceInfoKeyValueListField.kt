package com.mobeetest.worker.ui.activities.main.pages.composables.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mobeetest.worker.R
import com.mobeetest.worker.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Key-value table for lists like Security providers
 */
@ExperimentalFoundationApi
@Composable
fun DeviceInfoKeyValueListField(
    index: Int,
    iconRes: Int,
    label: String,
    pairs: Map<String, String>,
    modifier: Modifier = Modifier,
    maxPreviewItems: Int = 6,
    infoDescription: String? = null,
    showBottomDivider: Boolean = true
) {
    val context = LocalContext.current
    
    if (pairs.isEmpty()) {
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = label,
            value = stringResource(R.string.device_info_none),
            modifier = modifier,
            infoDescription = infoDescription
        )
        return
    }

    val clipboardManager = LocalClipboardManager.current
    var expanded by rememberSaveable(label + "_kv_expanded") { mutableStateOf(false) }
    var showInfo by rememberSaveable(label + index.toString() + "_kv_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(label + index.toString() + "_kv_copy") { mutableStateOf(false) }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val entries = pairs.entries.toList()
    val total = entries.size
    val itemsToShow = if (expanded || total <= maxPreviewItems) {
        entries
    } else {
        entries.take(maxPreviewItems)
    }
    val bgColor = deviceInfoFieldBackground(index)

    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val headerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    val tableBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val rowEven = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
    val rowOdd = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .bringIntoViewRequester(bringIntoViewRequester)
            .animateContentSize()
    ) {
        // Header row (index, icon, label, info, copy)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing12, top = deviceInfoSpacing6, bottom = deviceInfoSpacing6),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .width(deviceInfoFieldIndexColumnWidth),
                textAlign = TextAlign.End
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
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier.size(deviceInfoIconSize24).padding(end = deviceInfoSpacing2)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = context.getString(R.string.device_info_cd_info, label),
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    val copyText = buildString {
                        appendLine("$index.  $label")
                        entries.forEachIndexed { i, (k, v) ->
                            appendLine("  ${index}.${i + 1}  $k: $v")
                        }
                    }.trim()
                    clipboardManager.setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier.size(deviceInfoIconSize24).padding(end = deviceInfoSpacing2)
            ) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = context.getString(R.string.device_info_cd_copy, label),
                    tint = Color.Unspecified
                )
            }
        }

        // Copied message (BEFORE table)
        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing12, bottom = deviceInfoSpacing2),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(R.string.device_info_copied_to_clipboard),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Info tooltip (BEFORE table)
        if (infoDescription != null) {
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
                                shape = RoundedCornerShape(deviceInfoCornerRadius8)
                            )
                            .padding(horizontal = deviceInfoSpacing10, vertical = deviceInfoSpacing6)
                    ) {
                        Text(
                            text = infoDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }

            LaunchedEffect(showInfo) {
                if (showInfo) {
                    delay(4_000)
                    showInfo = false
                }
            }
        }

        // Table (AFTER info tooltip)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = deviceInfoSpacing16, vertical = deviceInfoSpacing4)
                .clip(RoundedCornerShape(deviceInfoCornerRadius8))
                .border(deviceInfoBorderThickness, outlineColor, RoundedCornerShape(deviceInfoCornerRadius8))
                .background(tableBg)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = deviceInfoSpacing8, vertical = deviceInfoSpacing4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.device_info_table_header_index),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(deviceInfoTableIndexColumnWidth),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.device_info_table_header_name),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = stringResource(R.string.device_info_table_header_value),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1.0f),
                        textAlign = TextAlign.End
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = deviceInfoDividerThickness,
                    color = outlineColor
                )

                itemsToShow.forEachIndexed { idx, (k, v) ->
                    val rowIndexLabel = "${index}.${idx + 1}"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (idx % 2 == 0) rowEven else rowOdd)
                            .padding(horizontal = deviceInfoSpacing8, vertical = deviceInfoSpacing4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rowIndexLabel,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(deviceInfoTableIndexColumnWidth),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = k,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.8f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = v,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1.0f),
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (idx < itemsToShow.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = deviceInfoTableDividerThickness,
                            color = outlineColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        LaunchedEffect(showCopied) {
            if (showCopied) {
                delay(1_500)
                showCopied = false
            }
        }

        if (total > maxPreviewItems) {
            TextButton(
                onClick = {
                    val newExpanded = !expanded
                    expanded = newExpanded
                    if (!newExpanded) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
                contentPadding = PaddingValues(horizontal = deviceInfoSpacing12, vertical = deviceInfoSpacing0)
            ) {
                Text(
                    text = if (expanded) {
                        stringResource(R.string.device_info_show_less)
                    } else {
                        context.getString(R.string.device_info_show_all, total)
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = deviceInfoDividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}
