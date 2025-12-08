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
fun DeviceInfoDateFieldRow(
    index: Int,
    iconRes: Int,
    label: String,
    millis: Long,
    infoDescription: String? = null,
    showBottomDivider: Boolean = true
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showInfo by remember { mutableStateOf(false) }
    var showCopied by remember { mutableStateOf(false) }

    val formattedDate = formatDate(millis)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(deviceInfoFieldBackground(index))
            .padding(horizontal = deviceInfoSpacing12, vertical = deviceInfoSpacing8),
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
                .size(deviceInfoIconSize24)
                .padding(end = deviceInfoSpacing8)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = deviceInfoFieldLabelTextStyle,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(deviceInfoSpacing4))

            Row(
                horizontalArrangement = Arrangement.spacedBy(deviceInfoSpacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = deviceInfoFieldValueTextStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                MiniMonthCalendar(millis = millis)
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
                        .size(deviceInfoIconSize24)
                        .padding(end = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = context.getString(R.string.device_info_cd_info, label),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(deviceInfoIconSize24)
                    )
                }
            }

            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString("$index. $label: $formattedDate"))
                    showCopied = true
                },
                modifier = Modifier
                    .size(deviceInfoIconSize24)
                    .padding(start = 2.dp)
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

    LaunchedEffect(key1 = showCopied) {
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
