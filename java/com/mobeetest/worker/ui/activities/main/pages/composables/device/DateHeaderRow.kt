package com.mobeetest.worker.ui.activities.main.pages.composables.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.mobeetest.worker.R
import com.mobeetest.worker.ui.theme.*
import kotlinx.coroutines.delay

@Suppress("unused")
@Composable
fun DateHeaderRow(
    index: Int,
    iconRes: Int,
    label: String,
    displayValue: String,
    infoDescription: String?,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var showInfo by rememberSaveable("${label}_${index}_date_header_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable("${label}_${index}_date_header_copy") { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header row (index, icon, label, info, copy)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = deviceInfoSpacing8,
                    end = deviceInfoSpacing12,
                    top = deviceInfoSpacing6,
                    bottom = deviceInfoSpacing6
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = deviceInfoFieldIndexTextStyle,
                modifier = Modifier.width(deviceInfoFieldIndexColumnWidth),
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

            val color = MaterialTheme.colorScheme.primary
            val titleText = remember(label, displayValue) {
                buildAnnotatedString {
                    append(label)
                    append(": ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    ) {
                        append(displayValue)
                    }
                }
            }

            Text(
                text = titleText,
                style = deviceInfoFieldLabelTextStyle,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier
                        .size(deviceInfoIconSize24)
                        .padding(end = deviceInfoSpacing2)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = stringResource(R.string.device_info_cd_info, label),
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    val copyText = "$index.  $label: $displayValue"
                    clipboardManager.setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier
                    .size(deviceInfoIconSize24)
                    .padding(end = deviceInfoSpacing2)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.copy),
                    contentDescription = stringResource(R.string.device_info_cd_copy, label),
                    tint = Color.Unspecified
                )
            }
        }

        // Copied message
        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = deviceInfoSpacing8,
                        end = deviceInfoSpacing12,
                        bottom = deviceInfoSpacing2
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(R.string.device_info_copied_to_clipboard),
                    style = deviceInfoCopiedMessageTextStyle,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Info tooltip
        if (infoDescription != null) {
            AnimatedVisibility(visible = showInfo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = deviceInfoSpacing12,
                            end = deviceInfoSpacing12,
                            bottom = deviceInfoSpacing4
                        )
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
                            style = deviceInfoInfoDescriptionTextStyle,
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

        LaunchedEffect(showCopied) {
            if (showCopied) {
                delay(1_500)
                showCopied = false
            }
        }
    }
}
