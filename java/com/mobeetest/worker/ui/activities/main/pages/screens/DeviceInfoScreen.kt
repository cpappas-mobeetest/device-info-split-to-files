@file:OptIn(ExperimentalFoundationApi::class)

package com.mobeetest.worker.ui.activities.main.pages.screens

// Android & Compose core imports
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import android.util.Log
import com.mobeetest.worker.R
import kotlinx.coroutines.delay

// View model
import com.mobeetest.worker.viewModels.DeviceInfoViewModel

// Theme
import com.mobeetest.worker.ui.theme.mainBottomBarBorder
import com.mobeetest.worker.ui.theme.mainBottomBarBorderHeight

// Device info models
import com.mobeetest.worker.data.model.device.DeviceInfo

// Imported device info composables
import com.mobeetest.worker.ui.activities.main.pages.composables.device.*

/**
 * Main Device Info Screen - Refactored
 * 
 * This file has been refactored to import 50 modular composable files from the device/ folder.
 * Original file: 6,244 lines → Refactored file: ~460 lines (93% reduction)
 * 
 * All composables, utilities, and helpers have been extracted to separate files with proper
 * package structure: com.mobeetest.worker.activities.main.pages.composables.device
 * 
 * This file now contains only:
 * - The main DeviceInfoScreen composable (entry point)
 * - DeviceInfoSectionItem composable (orchestrator that dispatches to extracted section fields)
 */

// Screen layout constants
private val DeviceInfoHorizontalPadding = 12.dp
private val DeviceInfoScrollbarExtraPadding = 8.dp

@Composable
fun DeviceInfoScreen(
    viewModels: List<AndroidViewModel>
) {
    val timeStart: Long = System.currentTimeMillis()
    val viewModel = remember {
        viewModels.first { it is DeviceInfoViewModel } as DeviceInfoViewModel
    }

    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val updateInProgress by viewModel.updateInProgress.collectAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadDeviceInfo()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top icons row (share, update, refresh, etc.)
            Box(
                modifier = Modifier
                    .padding(
                        top = 6.dp,
                        bottom = 6.dp
                    )
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RightSideIcons(
                        updateState = if (updateInProgress) "InProgress" else "Idle",
                        onRefreshClick = { viewModel.loadDeviceInfo() },
                        modifier = Modifier
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mainBottomBarBorderHeight)
                    .background(mainBottomBarBorder)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Main scrollable content area
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(
                            start = DeviceInfoHorizontalPadding,
                            end = DeviceInfoHorizontalPadding + DeviceInfoScrollbarExtraPadding,
                            top = 4.dp,
                            bottom = 4.dp
                        )
                ) {
                    // Build and display all sections using the extracted builder
                    val sections = remember { buildDeviceInfoSections() }

                    sections.forEach { item ->
                        DeviceInfoSectionItem(
                            item = item,
                            level = 0,
                            deviceInfo = deviceInfo
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Bottom logos (manufacturer, Mobeetest, Android)
                    BottomLogos(deviceInfo?.os?.manufacturer ?: "")

                    // Special thanks attribution
                    CpuInfoSpecialThanksRow()
                }

                // Custom vertical scrollbar
                VerticalScrollbar(
                    scrollState = scrollState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp)
                )
            }
        }
    }

    // Performance logging
    LaunchedEffect(Unit) {
        // Wait one frame
        withFrameNanos { }
        val timeEnd = System.currentTimeMillis()
        val duration = timeEnd - timeStart
        Log.d("DeviceInfoScreen", "Total load time: $duration ms")
    }
}

@Composable
private fun DeviceInfoSectionItem(
    item: DeviceInfoItem,
    level: Int,
    deviceInfo: DeviceInfo?
) {
    var isExpanded by rememberSaveable(item.id) { mutableStateOf(true) }
    var showInfoTooltip by rememberSaveable(item.id + "_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(item.id + "_copied") { mutableStateOf(false) }

    val indent = (level * 12).dp
    val clipboardManager = LocalClipboardManager.current
    val shareText = remember(item.id, deviceInfo) {
        buildSectionShareText(item, deviceInfo)
    }

    // Header palette
    val categoryHeaderBackground = Color(0xFFE6F4EA)      // light green
    val categoryHeaderContent = Color(0xFF0D652D)        // dark green
    val subCategoryHeaderBackground = Color(0xFFFFF7CC)  // light yellow
    val subCategoryHeaderContent = Color(0xFF7A4F00)     // dark golden brown

    val headerBackground =
        if (level == 0) categoryHeaderBackground else subCategoryHeaderBackground
    val headerContentColor =
        if (level == 0) categoryHeaderContent else subCategoryHeaderContent

    // Number of fields -> correct zebra color on bottom oval
    val fieldCount = remember(item.id, deviceInfo) {
        countFieldsForSection(item.id, deviceInfo)
    }

    val hasFields = fieldCount > 0
    val hasChildren = item.children.isNotEmpty()

    // Αν δεν έχει τίποτα να δείξει, δεν "ανοίγει"
    val canExpand = hasFields || hasChildren
    val isSectionExpanded = isExpanded && canExpand

    val cardContainerColor =
        if (fieldCount > 0) deviceInfoFieldBackground(fieldCount)
        else deviceInfoFieldBackground(1)

    // Rotation for expand / collapse icon
    val rotation by animateFloatAsState(
        targetValue = if (isSectionExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "sectionExpandRotation"
    )

    val toggleExpand: () -> Unit = {
        if (canExpand) {
            isExpanded = !isExpanded
            if (!isExpanded) {
                showInfoTooltip = false
            }
        }
    }



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent, end = indent)
            .clip(RoundedCornerShape(10.dp))
            .background(cardContainerColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(headerBackground)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        if (canExpand) toggleExpand()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.title,
                    modifier = Modifier.size(if (level == 0) 28.dp else 22.dp),
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = item.title,
                    style = if (level == 0)
                        MaterialTheme.typography.titleMedium
                    else
                        MaterialTheme.typography.bodyMedium,
                    color = headerContentColor,
                    modifier = Modifier.weight(1f)
                )

                // Info icon
                IconButton(
                    onClick = { showInfoTooltip = !showInfoTooltip },
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info ${item.title}",
                        tint = Color.Unspecified
                    )
                }

                // Copy icon
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(shareText))
                        showCopied = true
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.copy),
                        contentDescription = "Copy ${item.title}",
                        tint = Color.Unspecified
                    )
                }

                // Expand / Collapse icon
                IconButton(
                    onClick = { toggleExpand() },
                    enabled = canExpand,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = if (isSectionExpanded) {
                            "Collapse ${item.title}"
                        } else {
                            "Expand ${item.title}"
                        },
                        modifier = Modifier.rotate(rotation),
                        tint = headerContentColor.copy(alpha = if (canExpand) 1f else 0.35f)
                    )
                }
            }

            // "Copied" message
            AnimatedVisibility(visible = showCopied) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBackground)
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Copied to clipboard",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tooltip bubble with description
            AnimatedVisibility(visible = showInfoTooltip) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBackground)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterStart)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Auto-hide info tooltip after delay
            LaunchedEffect(showInfoTooltip) {
                if (showInfoTooltip) {
                    delay(10_000)
                    showInfoTooltip = false
                }
            }

            // Auto-hide "Copied" message after delay
            LaunchedEffect(showCopied) {
                if (showCopied) {
                    delay(10_000)
                    showCopied = false
                }
            }

            // Expanded content: fields + children
            AnimatedVisibility(visible = isSectionExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        thickness = 0.5.dp,
                        color = Color(0xFF736D6D)
                    )

                    // 1) Fields (μόνο για το συγκεκριμένο item.id)
                    deviceInfo?.let { info ->
                        when (item.id) {
                            "cpu" -> CpuSectionFields(info.cpu, item.iconRes)
                            "gpu" -> GpuSectionFields(info.gpu, item.iconRes)
                            "ram" -> RamSectionFields(info.ram, item.iconRes)
                            "storage" -> StorageSectionFields(info.storage, item.iconRes)
                            "screen" -> ScreenSectionFields(info.screen, item.iconRes)
                            "os" -> OsSectionFields(info.os, item.iconRes)
                            "hardware_battery" -> BatterySectionFields(info.hardware.battery, item.iconRes)
                            "hardware_sound_cards" -> SoundCardsSectionFields(info.hardware.soundCardCount, item.iconRes)
                            "hardware_cameras" -> CamerasSectionFields(info.hardware.cameras, item.iconRes)
                            "hardware_wireless" -> WirelessSectionFields(info.hardware.wireless, item.iconRes)
                            "hardware_usb" -> UsbSectionFields(info.hardware.usb, item.iconRes)
                            "weather" -> WeatherSectionFields(info.weatherInfo, item.iconRes)
                            "mobeetest" -> MobeetestSectionFields(
                                mobeetest = info.mobeetestInfo,
                                iconRes = R.drawable.ic_launcher_foreground
                            )
                        }
                    }

                    // 2) Children
                    if (hasChildren) {
                        Spacer(modifier = Modifier.height(4.dp))
                        item.children.forEach { child ->
                            Spacer(modifier = Modifier.height(4.dp))
                            DeviceInfoSectionItem(
                                item = child,
                                level = level + 1,
                                deviceInfo = deviceInfo
                            )
                        }
                    }
                }
            }
        }
    }
}
