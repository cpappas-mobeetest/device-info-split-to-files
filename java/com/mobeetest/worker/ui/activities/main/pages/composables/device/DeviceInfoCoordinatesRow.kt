package com.mobeetest.worker.ui.activities.main.pages.composables.device

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import com.mobeetest.worker.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DeviceInfoCoordinatesRow(
    index: Int,
    iconRes: Int,
    label: String,
    latitude: Double?,
    longitude: Double?,
    modifier: Modifier = Modifier,
    infoDescription: String? = null,
    showBottomDivider: Boolean = true
) {
    val bgColor = deviceInfoFieldBackground(index)
    val coordinatesValue = "${latitude ?: "?"}, ${longitude ?: "?"}"
    val googleMapsUrl = if (latitude != null && longitude != null) {
        "https://www.google.com/maps?q=$latitude,$longitude"
    } else {
        null
    }

    var showInfoTooltip by rememberSaveable { mutableStateOf(false) }
    var showCopied by rememberSaveable { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(showCopied) {
        if (showCopied) {
            delay(1_500)
            showCopied = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(deviceInfoFieldIndexColumnWidth),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            // Icon
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(29.dp)
                    .padding(start = 8.dp, end = 4.dp),
                tint = Color.Unspecified
            )

            // Label and coordinates inline
            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    append(label)
                    append(": ")
                    withStyle(
                        androidx.compose.ui.text.SpanStyle(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append(coordinatesValue)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            // Location icon (clickable) in 80dp box
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(horizontal = 4.dp, vertical = 8.dp)
                    .clickable(enabled = googleMapsUrl != null) {
                        googleMapsUrl?.let { uriHandler.openUri(it) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.location),
                    contentDescription = "Open in Google Maps",
                    modifier = Modifier.size(26.dp),
                    tint = if (googleMapsUrl != null) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Info icon
            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfoTooltip = !showInfoTooltip },
                    modifier = Modifier.size(24.dp).padding(end = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            // Copy icon
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(coordinatesValue))
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

        // Info tooltip
        if (showInfoTooltip && infoDescription != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Spacer(modifier = Modifier.width(deviceInfoIconSize26 + deviceInfoSpacing12))
                Text(
                    text = infoDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Copied message
        if (showCopied) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Copied!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Bottom divider
        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = deviceInfoBorderThicknessHalf,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}
