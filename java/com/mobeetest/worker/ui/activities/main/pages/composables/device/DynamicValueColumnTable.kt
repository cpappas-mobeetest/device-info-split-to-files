package com.mobeetest.worker.ui.activities.main.pages.composables.device

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.data.model.device.StorageInfo
import com.mobeetest.worker.ui.theme.TableIndexColumnWidth
import java.util.Locale

@Composable
fun DynamicValueColumnTable(
    index: Int,
    rows: List<Pair<String, String>>,
    storage: StorageInfo,
    headerBg: Color,
    rowEven: Color,
    rowOdd: Color,
    outlineColor: Color
) {
    val density = LocalDensity.current

    SubcomposeLayout { constraints ->
        // Step 1: Measure all value cells to find max width
        val valuePlaceables = subcompose("measure_values") {
            // Header value
            Text(
                text = "Value",
                style = MaterialTheme.typography.labelSmall
            )

            // All row values
            rows.forEach { (title, value) ->
                if (title == "Used space") {
                    Box(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
                    ) {
                        Box(modifier = Modifier.size(96.dp))
                    }
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }.map { it.measure(androidx.compose.ui.unit.Constraints()) }

        val maxValueWidthPx = valuePlaceables.maxOfOrNull { it.width } ?: 0
        val numberWidthPx = with(density) { TableIndexColumnWidth.roundToPx() }
        val startPaddingPx = with(density) { 8.dp.roundToPx() }
        val endPaddingPx = with(density) { 8.dp.roundToPx() }

        val availableForTable = constraints.maxWidth - startPaddingPx - endPaddingPx
        val valueColumnWidthPx = maxValueWidthPx.coerceAtMost(availableForTable - numberWidthPx - 100)
        val titleColumnWidthPx = availableForTable - numberWidthPx - valueColumnWidthPx

        // Step 2: Compose actual table with calculated width
        val tablePlaceables = subcompose("actual_table") {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(TableIndexColumnWidth),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(with(density) { titleColumnWidthPx.toDp() })
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier.width(with(density) { valueColumnWidthPx.toDp() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Value",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = outlineColor
                )

                // Rows
                rows.forEachIndexed { idx, (title, value) ->
                    val rowIndexLabel = "${index}.${idx + 1}"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (idx % 2 == 0) rowEven else rowOdd)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rowIndexLabel,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(TableIndexColumnWidth),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(with(density) { titleColumnWidthPx.toDp() }),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Box(
                                modifier = Modifier.width(with(density) { valueColumnWidthPx.toDp() }),
                                contentAlignment = Alignment.Center
                            ) {
                                if (title == "Used space") {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier.size(96.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val clamped = storage.percentUsed.toFloat().coerceIn(0f, 100f)
                                            val valueText = String.format(Locale.US, "%.3f%%", clamped)

                                            PercentageDonut(
                                                percentage = clamped,
                                                modifier = Modifier.fillMaxSize(),
                                                baseColor = MaterialTheme.colorScheme.surfaceVariant,
                                                progressColor = MaterialTheme.colorScheme.primary,
                                                strokeWidth = 6.dp,
                                                label = valueText
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (idx < rows.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.25.dp,
                            color = outlineColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }.map { it.measure(constraints) }

        val tableHeight = tablePlaceables.firstOrNull()?.height ?: 0

        layout(constraints.maxWidth, tableHeight) {
            tablePlaceables.firstOrNull()?.placeRelative(0, 0)
        }
    }
}
