package com.mobeetest.worker.ui.activities.main.pages.composables.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun WeatherAlignedVisibilityRow(
    index: Int,
    iconRes: Int,
    label: String,
    visibilityKm: Double,
    visMiles: Double,
    infoDescription: String?,
    showBottomDivider: Boolean = true
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val bgColor = deviceInfoFieldBackground(index)

    var showInfo by rememberSaveable("${label}_${index}_visibility_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable("${label}_${index}_visibility_copy") { mutableStateOf(false) }

    val valueText = String.format(Locale.US, "%.1f km (%.1f mi)", visibilityKm, visMiles)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        // First line: index, icon, title with bold value, spacer, info, copy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing12, top = deviceInfoSpacing6, bottom = deviceInfoSpacing6),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(deviceInfoFieldIndexColumnWidth),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(deviceInfoIconSize29)
                    .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing4)
            )

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
                        append(valueText)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 6,
                overflow = androidx.compose.ui.text.style.TextOverflow.Clip
            )


            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (infoDescription != null) {
                    IconButton(
                        onClick = { showInfo = !showInfo },
                        modifier = Modifier.size(deviceInfoIconSize24).padding(end = 2.dp)
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
                        clipboardManager.setText(AnnotatedString("$index. $label: $valueText"))
                        showCopied = true
                    },
                    modifier = Modifier.size(deviceInfoIconSize24).padding(start = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.copy),
                        contentDescription = context.getString(R.string.device_info_cd_copy, label),
                        tint = Color.Unspecified
                    )
                }
            }
        }

        // Second line: MiniVisibilityBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing12, bottom = deviceInfoSpacing8),
            contentAlignment = Alignment.Center
        ) {
            MiniVisibilityBar(
                visibilityKm = visibilityKm,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
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
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(deviceInfoCornerRadius8)
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

        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = deviceInfoBorderThickness,
                color = deviceInfoDivider
            )
        }
    }
}
