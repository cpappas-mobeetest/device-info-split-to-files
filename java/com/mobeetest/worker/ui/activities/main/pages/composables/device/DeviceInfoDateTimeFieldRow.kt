package com.mobeetest.worker.ui.activities.main.pages.composables.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import kotlinx.coroutines.delay

@Composable
fun DeviceInfoDateTimeFieldRow(
    index: Int,
    iconRes: Int,
    label: String,
    millis: Long,
    modifier: Modifier = Modifier,
    valueOverride: String? = null,
    infoDescription: String? = null,
    showBottomDivider: Boolean = true
) {
    val bgColor = deviceInfoFieldBackground(index)
    val displayValue = valueOverride ?: formatDateTime(millis)

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showInfo by remember { mutableStateOf(false) }
    var showCopied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        // Use common header
        DateHeaderRow(
            index = index,
            iconRes = iconRes,
            label = label,
            displayValue = displayValue,
            infoDescription = infoDescription,
            onInfoClick = { showInfo = !showInfo },
            onCopyClick = {
                clipboardManager.setText(AnnotatedString("$index. $label: $displayValue"))
                showCopied = true
            }
        )

        // Calendar + Clock body
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniMonthCalendar(
                millis = millis,
                modifier = Modifier.weight(1f)
            )
            MiniAnalogClock(
                millis = millis,
                modifier = Modifier.weight(0.6f)
            )
        }

        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = deviceInfoBorderThicknessHalf,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }

    // Info tooltip separate from the Column to avoid layout issues
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

    LaunchedEffect(showCopied) {
        if (showCopied) {
            delay(1_500)
            showCopied = false
        }
    }
}
