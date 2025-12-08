package com.mobeetest.worker.ui.activities.permissions.pages.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.permissions.PermissionStatus

@Composable
fun PermissionStepIndicator(
    permissionName: String,
    status: PermissionStatus,
    baseSize: Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "permissionAnim")

    // Pulsing scale μόνο όταν Requesting
    val animatedScale = if (status == PermissionStatus.Requesting) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scaleAnim"
        ).value
    } else 1f

    // ⛔ ΑΦΑΙΡΕΘΗΚΕ η animatedRotation – δεν θέλουμε να περιστρέφεται το εικονίδιο
    // val animatedRotation = ...

    val baseColor = when (status) {
        PermissionStatus.Requesting -> colorResource(id = R.color.materialPurple500)
        PermissionStatus.Granted -> colorResource(id = R.color.materialBlue600)
        PermissionStatus.Denied -> colorResource(id = R.color.materialErrorRed)
        PermissionStatus.NotChecked -> colorResource(id = R.color.statusGray)
    }

    val animatedColor by animateColorAsState(
        targetValue = baseColor,
        animationSpec = tween(durationMillis = 1000),
        label = "colorAnim"
    )

    val density = LocalDensity.current
    val sizePx = with(density) { baseSize.toPx() }
    val strokeWidthPx = with(density) { 4.dp.toPx() }
    val gapPx = with(density) { 1.dp.toPx() }

    val gradientBrush = Brush.radialGradient(
        colors = listOf(animatedColor, animatedColor.copy(alpha = 0.5f)),
        center = Offset(x = sizePx / 2, y = sizePx / 2),
        radius = sizePx / 2
    )

    val showCustomDrawable = status == PermissionStatus.NotChecked || status == PermissionStatus.Requesting

    val icon = when (status) {
        PermissionStatus.Granted -> Icons.Filled.CheckCircle
        PermissionStatus.Denied -> Icons.Filled.Cancel
        else -> null
    }

    val customDrawableRes = when (permissionName) {
        "Battery" -> R.drawable.power
        "Location" -> R.drawable.location
        "Notifications" -> R.drawable.notification
        "Phone" -> R.drawable.read_phone_state
        "Background" -> R.drawable.background_location
        "Pause" -> R.drawable.pause_app_activity
        "Camera" -> R.drawable.camera
        "Wifi" -> R.drawable.wifi
        else -> null
    }

    val displayText = displayNameFor(permissionName) + " - " + status.toString()
    val ringThickness = 4.dp
    val gap = 1.dp
    val totalSize = (baseSize.value + 2 * (ringThickness.value + gap.value)).dp

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
        Box(
            modifier = Modifier
                .size(totalSize)
                .scale(animatedScale)
                .drawBehind {
                    val ringRadius = sizePx / 2 + gapPx + strokeWidthPx / 2
                    drawCircle(
                        brush = gradientBrush,
                        center = center,
                        radius = ringRadius,
                        style = Stroke(width = strokeWidthPx)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .padding(ringThickness + gap)
                    .size(baseSize)
                    .background(brush = gradientBrush, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (showCustomDrawable && customDrawableRes != null) {
                    // ✅ Χωρίς περιστροφή πλέον
                    Image(
                        painter = painterResource(id = customDrawableRes),
                        contentDescription = "$permissionName icon",
                        colorFilter = ColorFilter.tint(colorResource(id = R.color.white)),
                        modifier = Modifier.fillMaxSize(0.6f)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$permissionName status",
                        tint = colorResource(id = R.color.white),
                        modifier = Modifier.fillMaxSize(0.6f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(baseSize)
        )
    }
}

@Composable
fun displayNameFor(permission: String): String = when (permission) {
    "Battery" -> stringResource(R.string.permissions_screen_battery_full)
    "Location" -> stringResource(R.string.permissions_screen_location_full)
    "Notifications" -> stringResource(R.string.permissions_screen_notifications_full)
    "Phone" -> stringResource(R.string.permissions_screen_phone_full)
    "Background" -> stringResource(R.string.permissions_screen_background_full)
    "Pause" -> stringResource(R.string.permissions_screen_pause_full)
    "Camera" -> stringResource(R.string.permissions_screen_camera_full)
    "Wifi" -> stringResource(R.string.permissions_screen_wifi_full)
    else -> permission
}
