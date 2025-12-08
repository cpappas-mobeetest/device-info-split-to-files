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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun WeatherAlignedTemperatureRow(
    index: Int,
    iconRes: Int,
    label: String,
    temperatureC: Double,
    textWidth: Dp,
    visualWidth: Dp,
    infoDescription: String?,
    thermometerHeight: Dp = 64.dp
) {
    val clipboardManager = LocalClipboardManager.current
    val bgColor = deviceInfoFieldBackground(index)

    var showInfo by rememberSaveable("${label}_${index}_temp_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable("${label}_${index}_temp_copy") { mutableStateOf(false) }

    val valueText = String.format(Locale.US, "%.1f °C", temperatureC)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(deviceInfoFieldIndexWidth),
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

            Box(
                modifier = Modifier.width(textWidth),
                contentAlignment = Alignment.CenterStart
            ) {
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .width(visualWidth)
                    .height(thermometerHeight),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(width = 26.dp, height = thermometerHeight)
                ) {
                    ThermometerMini(
                        temperatureC = temperatureC,
                        modifier = Modifier.fillMaxSize()
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
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.information),
                            contentDescription = "Info $label",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString("$index. $label: $valueText"))
                        showCopied = true
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.copy),
                        contentDescription = "Copy $label",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, bottom = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.bodySmall,
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
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
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

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = deviceInfoBorderThickness,
            color = deviceInfoDivider
        )
    }
}
