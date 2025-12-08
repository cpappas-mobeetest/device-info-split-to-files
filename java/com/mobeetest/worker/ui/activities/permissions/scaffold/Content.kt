package com.mobeetest.worker.ui.activities.permissions.scaffold

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withSave
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.permissions.PermissionStep
import com.mobeetest.worker.ui.activities.permissions.pages.composables.PermissionStepIndicator
import com.mobeetest.worker.ui.theme.*
import kotlin.math.sin

@Composable
fun SineWaveDivider(
    modifier: Modifier = Modifier,
    color: Color = bootStrapAlertSuccessSeperatorColor,
    thickness: androidx.compose.ui.unit.Dp = bootStrapAlertSuccessSeperatorThickness,
    amplitudeDp: androidx.compose.ui.unit.Dp = 6.dp,
    cycles: Float = 2f,
    phaseRadians: Float = 0f,
    verticalOffsetDp: androidx.compose.ui.unit.Dp = 0.dp
) {
    val defaultHeight = (amplitudeDp * 2f) + thickness + 4.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(defaultHeight)
            .clip(RoundedCornerShape(0.dp))
    ) {
        val strokePx = thickness.toPx()
        val amplitudePx = amplitudeDp.toPx()
        val verticalOffsetPx = verticalOffsetDp.toPx()

        val w = size.width
        val h = size.height
        val centerY = h / 2f + verticalOffsetPx
        val twoPi = (2.0 * Math.PI).toFloat()

        val path = Path()
        val step = maxOf(1f, minOf(2f, w / 600f))
        var x = 0f
        var first = true
        while (x <= w) {
            val t = x / w
            val y = centerY + amplitudePx * sin(cycles * twoPi * t + phaseRadians)
            if (first) { path.moveTo(x, y); first = false } else { path.lineTo(x, y) }
            x += step
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Content(
    modifier: Modifier,
    progressMessage: String,
    completedSteps: Int,
    totalSteps: Int,
    permissionSteps: List<PermissionStep>
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier
    ) {

        // MAIN CONTENT (scrolls)
        Row(
            modifier = Modifier
                .weight(1f)            // 🟢 Σπρώχνει ΟΤΙ ΥΠΑΡΧΕΙ προς τα κάτω
                .fillMaxWidth()
        ) {
            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(permissionsScaffoldContentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Alert Box
                    Box(
                        modifier = Modifier
                            .padding(permissionsScaffoldContentAlertBox1Padding)
                            .clip(
                                RoundedCornerShape(
                                    permissionsScaffoldContentAlertBox1RoundedCornerShape
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    val shadowRadius = 24f
                                    val shadowOffsetX = 0f
                                    val shadowOffsetY = 0f
                                    drawIntoCanvas {
                                        val paint = Paint().apply {
                                            this.color = bootStrapAlertSuccessShadowColor
                                            this.asFrameworkPaint().apply {
                                                isAntiAlias = true
                                                setShadowLayer(
                                                    shadowRadius,
                                                    shadowOffsetX,
                                                    shadowOffsetY,
                                                    bootStrapAlertSuccessShadowColor.toArgb()
                                                )
                                            }
                                        }
                                        drawContext.canvas.nativeCanvas.apply {
                                            withSave {
                                                drawRoundRect(
                                                    0f,
                                                    0f,
                                                    size.width,
                                                    size.height,
                                                    20f,
                                                    20f,
                                                    paint.asFrameworkPaint()
                                                )
                                            }
                                        }
                                    }
                                }
                                .background(
                                    bootStrapAlertSuccess,
                                    RoundedCornerShape(
                                        permissionsScaffoldContentAlertBox1RoundedCornerShape
                                    )
                                )
                                .border(
                                    permissionsScaffoldContentAlertBox1Border,
                                    bootStrapAlertSuccessBorder,
                                    RoundedCornerShape(
                                        permissionsScaffoldContentAlertBox1RoundedCornerShape
                                    )
                                )
                                .padding(permissionsScaffoldContentAlertBox1ShadowPadding)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // 1. Progress Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (completedSteps < totalSteps) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .padding(
                                                    permissionsScaffoldContentCircularProgressIndicatorPadding
                                                )
                                                .size(
                                                    permissionsScaffoldContentCircularProgressIndicatorSize
                                                ),
                                            strokeWidth = permissionsScaffoldContentCircularProgressIndicatorStrokeWidth,
                                            color = permissionsScaffoldContentCircularProgressIndicatorColor
                                        )
                                    }
                                    Text(
                                        text = progressMessage,
                                        style = permissionsScaffoldContentProgressMessageTypography.bodyLarge,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // 2. Sine Wave Divider
                                SineWaveDivider(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = bootStrapAlertSuccessSeperatorColor,
                                    thickness = bootStrapAlertSuccessSeperatorThickness,
                                    amplitudeDp = 1.dp,
                                    cycles = 50f
                                )

                                // 3. Information Text
                                Text(
                                    text = stringResource(R.string.permissions_check_necessary_permissions),
                                    style = permissionsScaffoldContentInfoMessageTypography.bodyLarge,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Blue & Red Circles with Permission Names
                    @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(permissionsScaffoldContentStepIndicatorAreaPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        val maxScaleFactor = 1.2f
                        val effectiveSize =
                            (maxWidth - permissionsScaffoldContentStepIndicatorAreaSpacing * (permissionsScaffoldContentStepIndicatorItemsPerRow - 1)) / permissionsScaffoldContentStepIndicatorItemsPerRow
                        val baseSize = (effectiveSize / ((1 / 0.8f) * maxScaleFactor))
                        val permissionStepsChunked =
                            permissionSteps.chunked(
                                permissionsScaffoldContentStepIndicatorItemsPerRow
                            )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            permissionStepsChunked.forEachIndexed { index, rowChunk ->
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(
                                            permissionsScaffoldContentStepIndicatorAreaSpacing
                                        )
                                    ) {
                                        rowChunk.forEach { step ->
                                            PermissionStepIndicator(
                                                permissionName = step.name,
                                                status = step.status,
                                                baseSize = baseSize
                                            )
                                        }
                                    }
                                }
                                if (index < permissionStepsChunked.lastIndex) {
                                    Spacer(
                                        modifier = Modifier.height(
                                            permissionsScaffoldContentStepIndicatorAreaSpacing
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM SEPARATOR touching nav bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerLineColor)
        )
    }
}