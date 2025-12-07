package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun WeatherAlignedHumidityRow(
    index: Int,
    iconRes: Int,
    label: String,
    percentage: Float,
    textWidth: Dp,
    visualWidth: Dp,
    infoDescription: String?
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val bgColor = deviceInfoFieldBackground(index)

    var showInfo by rememberSaveable("${label}_${index}_humidity_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable("${label}_${index}_humidity_copy") { mutableStateOf(false) }

    val valueText = String.format(Locale.US, "%.3f%%", percentage)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing12, top = deviceInfoSpacing8, bottom = deviceInfoSpacing8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = deviceInfoFieldIndexTextStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(deviceInfoFieldIndexWidth)
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(deviceInfoIconSize29)
                    .padding(start = deviceInfoSpacing8, end = deviceInfoSpacing4)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = deviceInfoFieldLabelTextStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(deviceInfoSpacing4))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = valueText,
                        style = deviceInfoFieldValueTextStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(textWidth)
                    )

                    Spacer(modifier = Modifier.width(deviceInfoSpacing8))

                    PercentageDonut(
                        percentage = percentage,
                        size = deviceInfoDonutSize,
                        strokeWidth = deviceInfoDonutStrokeWidth,
                        modifier = Modifier.width(visualWidth)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
                                shape = deviceInfoCornerRadius8
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

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = deviceInfoBorderThickness,
            color = deviceInfoDivider
        )
    }
}
