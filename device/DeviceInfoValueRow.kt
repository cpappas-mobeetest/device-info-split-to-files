package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import com.mobeetest.worker.ui.theme.deviceInfoFieldIndexColumnWidth
import kotlinx.coroutines.delay

@Composable
fun DeviceInfoValueRow(
    index: Int,
    iconRes: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    infoDescription: String? = null,
    showBottomDivider: Boolean = true
) {
    val clipboardManager = LocalClipboardManager.current
    val bgColor = deviceInfoFieldBackground(index)
    var showInfo by rememberSaveable(label + index.toString() + "_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(label + index.toString() + "_copy") { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
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
                    .size(29.dp)
                    .padding(start = 8.dp, end = 4.dp),
                tint = Color.Unspecified
            )

            val valueText = if (unit.isNullOrBlank()) value else "$value $unit"

            Text(
                text = buildAnnotatedString {
                    append(label)
                    append(": ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append(valueText)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 6,
                overflow = TextOverflow.Clip
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier.size(24.dp).padding(end = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    clipboardManager.setText(
                        AnnotatedString("$index. $label: $valueText")
                    )
                    showCopied = true
                },
                modifier = Modifier.size(24.dp).padding(end = 2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = "Copy $label",
                    tint = Color.Unspecified
                )
            }
        }

        // Copied message
        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, bottom = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (infoDescription != null) {
            AnimatedVisibility(visible = showInfo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
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

        LaunchedEffect(showCopied) {
            if (showCopied) {
                delay(1_500)
                showCopied = false
            }
        }
        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}
