package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobeetest.worker.R
import com.mobeetest.worker.models.StorageInfo

@Composable
fun DynamicValueColumnTable(
    index: Int,
    rows: List<Triple<String, String, Float?>>,
    storage: StorageInfo,
    headerBg: Color,
    rowEven: Color,
    rowOdd: Color,
    outlineColor: Color
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val valueColumnWidth = remember(rows, textMeasurer) {
        val textStyle = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        val maxValueWidthPx = rows.maxOf { (_, value, _) ->
            textMeasurer.measure(
                text = value,
                style = textStyle,
                maxLines = 1
            ).size.width
        }

        with(density) { maxValueWidthPx.toDp() }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(horizontal = deviceInfoSpacing8, vertical = deviceInfoSpacing6),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.device_info_table_header_name),
                style = deviceInfoTableHeaderTextStyle,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = stringResource(R.string.device_info_table_header_value),
                style = deviceInfoTableHeaderTextStyle,
                modifier = Modifier.width(valueColumnWidth),
                textAlign = TextAlign.End
            )
        }

        HorizontalDivider(
            thickness = deviceInfoBorderThickness,
            color = outlineColor
        )

        // Data rows
        rows.forEachIndexed { rowIndex, (name, value, percentage) ->
            val rowBg = if (rowIndex % 2 == 0) rowEven else rowOdd

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBg)
                    .padding(horizontal = deviceInfoSpacing8, vertical = deviceInfoSpacing6),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = deviceInfoTableCellTextStyle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (percentage != null) {
                        Spacer(modifier = Modifier.height(deviceInfoSpacing4))
                        PercentageDonut(
                            percentage = percentage,
                            size = deviceInfoIconSize24,
                            strokeWidth = 3.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(deviceInfoSpacing8))

                Text(
                    text = value,
                    style = deviceInfoTableCellTextStyle.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.width(valueColumnWidth),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (rowIndex < rows.lastIndex) {
                HorizontalDivider(
                    thickness = deviceInfoBorderThicknessHalf,
                    color = outlineColor.copy(alpha = 0.3f)
                )
            }
        }
    }
}
