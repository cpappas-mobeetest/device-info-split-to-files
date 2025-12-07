@file:OptIn(ExperimentalFoundationApi::class)

package com.mobeetest.worker.ui.activities.main.pages.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import com.mobeetest.worker.R
import com.mobeetest.worker.viewModels.DeviceInfoViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.mobeetest.worker.data.model.device.*
import java.util.Locale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip

import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mobeetest.worker.ui.theme.mainBottomBarBorder
import com.mobeetest.worker.ui.theme.mainBottomBarBorderHeight

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import kotlinx.coroutines.launch
import org.json.JSONObject

import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.cos
import kotlin.math.sin

import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

// Material / icons
import java.util.Calendar



import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.ripple


private enum class MobeetestRowType { TEXT, DATE, DATETIME }

private data class MobeetestRowSpec(
    val label: String,
    val value: String,
    val infoDescription: String? = null,
    val type: MobeetestRowType = MobeetestRowType.TEXT,
    val millis: Long? = null
)

private fun formatDate(millis: Long): String {
    if (millis <= 0L) return "Unknown"
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
}



private fun formatDateTime(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun buildMobeetestRows(info: MobeetestInfo?): List<MobeetestRowSpec> {
    if (info == null) return emptyList()

    val rows = mutableListOf<MobeetestRowSpec>()

    val flavorLabel = when (info.flavor) {
        "paid" -> "Paid edition"
        "web_campaign" -> "Web campaign edition"
        else -> "Free edition"
    }

    rows += MobeetestRowSpec(
        label = "Edition",
        value = flavorLabel,
        infoDescription = "Which Mobeetest edition is currently unlocked on this device."
    )

    info.version.takeIf { it.isNotBlank() }?.let { version ->
        rows += MobeetestRowSpec(
            label = "Version",
            value = version,
            infoDescription = "Human-readable app version shown to users (e.g., 1.2.3). Provided from BuildConfig."
        )
    }

    info.installerPackage?.takeIf { it.isNotBlank() }?.let { installer ->
        rows += MobeetestRowSpec(
            label = "Installer package",
            value = installer,
            infoDescription = "Package that installed this APK (e.g. Google Play or sideload)."
        )
    }

    rows += MobeetestRowSpec(
        label = "Installed on",
        value = formatDateTime(info.dateInstalled),
        infoDescription = "Timestamp when the app was first installed on the device.",
        type = MobeetestRowType.DATETIME,
        millis = info.dateInstalled
    )

    rows += MobeetestRowSpec(
        label = "Last updated on",
        value = formatDateTime(info.dateLastUpdated),
        infoDescription = "Timestamp when the app was last updated on the device.",
        type = MobeetestRowType.DATETIME,
        millis = info.dateLastUpdated
    )

    info.totalUpdates.let { updates ->
        rows += MobeetestRowSpec(
            label = "Total updates",
            value = updates.toString(),
            infoDescription = "How many different versions of the app have been installed since first install (counted locally)."
        )
    }

    info.totalApplicationRunsSinceFirstInstall.let { runs ->
        rows += MobeetestRowSpec(
            label = "Total runs",
            value = runs.toString(),
            infoDescription = "How many times the app has been started since first install (tracked locally)."
        )
    }

    val lastRun = info.lastRunAt ?: 0L

    rows += MobeetestRowSpec(
        label = "Last run at",
        value = formatDateTime(lastRun),
        infoDescription = "Timestamp of the most recent application run (based on local tracking).",
        type = MobeetestRowType.DATETIME,
        millis = lastRun
    )


    info.totalActiveServices.let { active ->
        rows += MobeetestRowSpec(
            label = "Active services",
            value = active.toString(),
            infoDescription = "Number of Mobeetest background services that are currently running."
        )
    }


    info.fcmToken?.takeIf { it.isNotBlank() }?.let { token ->
        rows += MobeetestRowSpec(
            label = "FCM token",
            value = token,
            infoDescription = "Current Firebase Cloud Messaging registration token used for push notifications."
        )
    }

    info.fcmUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
        rows += MobeetestRowSpec(
            label = "FCM UUID",
            value = uuid,
            infoDescription = "Stable UUID used to identify this installation for backend / push logic."
        )
    }

    info.ftpAvailableStorage?.takeIf { it > 0L }?.let { ftpBytes ->
        val mb = ftpBytes / (1024 * 1024)
        rows += MobeetestRowSpec(
            label = "FTP available storage",
            value = "$mb MB",
            infoDescription = "Free space reported by the configured FTP storage target (paid editions only)."
        )
    }

    rows += MobeetestRowSpec(
        label = "Build Type",
        value = info.buildType,
        infoDescription = "Gradle build type used for this APK (debug / release). Useful for identifying test vs production builds."
    )

    rows += MobeetestRowSpec(
        label = "Application id",
        value = info.applicationId,
        infoDescription = "Unique application package name installed on the device. Used by Android and backend systems to identify this app."
    )

    /*
    rows += MobeetestRowSpec(
        label = "Version name",
        value = info.versionName,
        infoDescription = "Human-readable app version shown to users (e.g., 1.2.3). Provided from BuildConfig."
    )
     */




    return rows
}




/**
 * Κεντρική οθόνη Device Info.
 *
 * - Full width κουμπί "Update Device Info" εκτός scroll.
 * - Από κάτv scroll area με categories + subcategories.
 */

private val DeviceInfoHorizontalPadding = 12.dp
private val DeviceInfoScrollbarExtraPadding = 8.dp

@Composable
private fun PlayGifAtLeastWhileInProgress(
    @DrawableRes resId: Int,
    @Suppress("SameParameterValue") minPlays: Int,
    inProgress: Boolean,
    modifier: Modifier = Modifier,
    @Suppress("SameParameterValue") contentDescription: String? = null,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val onFinishedState = rememberUpdatedState(onFinished)
    val inProgressState = rememberUpdatedState(inProgress)
    val minPlaysState = rememberUpdatedState(minPlays)


    var playsDone by remember(resId) { mutableIntStateOf(0) }

    val drawable = remember(resId) {
        val source = ImageDecoder.createSource(context.resources, resId)
        ImageDecoder.decodeDrawable(source) as? AnimatedImageDrawable
    }

    // όταν ξεκινάει νέο update, μηδένισε μετρητή
    LaunchedEffect(inProgress) {
        if (inProgress) playsDone = 0
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageDrawable(drawable)
                contentDescription?.let { this.contentDescription = it }
            }
        },
        update = { imageView ->
            imageView.setImageDrawable(drawable)
        }
    )

    DisposableEffect(drawable) {
        if (drawable == null) {
            onFinishedState.value()
            onDispose { }
        } else {
            // Παίζουμε ΜΙΑ φορά κάθε start και στο end αποφασίζουμε αν θα ξαναρχίσει.
            drawable.repeatCount = 0

            val callback = object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(d: Drawable?) {
                    playsDone++

                    val shouldContinue =
                        inProgressState.value || playsDone < minPlaysState.value

                    if (shouldContinue) {
                        drawable.start() // άλλη μία πλήρης γύρα
                    } else {
                        onFinishedState.value()
                    }
                }
            }

            drawable.registerAnimationCallback(callback)
            drawable.start()

            onDispose {
                drawable.unregisterAnimationCallback(callback)
                drawable.stop()
            }
        }
    }

}


@Composable
fun ActionIconSlot(
    touchSize: Dp,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(touchSize)
            .clip(CircleShape) // ✅ κυκλικό ripple
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}



@Composable
fun RightSideIcons(
    viewModel: DeviceInfoViewModel,
    updateInProgress: Boolean
) {
    val iconSize = 26.dp

    // ✅ Κοινό/αυξημένο touch για ΟΛΑ (και άρα ίδιο ripple)
    val touchSize = iconSize + 8.dp

    // ✅ Update οπτικά μεγαλύτερο
    val updateVisualSize = iconSize + 8.dp

    val icons = remember {
        listOf(
            // 1️⃣ Δεδομένα / formats (από πιο "raw" σε πιο "τελικό")
            R.drawable.json,
            R.drawable.csv,
            R.drawable.ms_excel,
            R.drawable.ms_word,
            R.drawable.pdf,
            R.drawable.png,

            // 2️⃣ Συσκευασία / μεταφορά
            R.drawable.zip,
            R.drawable.ftp,

            // 3️⃣ Ενέργειες πάνω στο αποτέλεσμα
            R.drawable.copy,

            // 4️⃣ Γενικές ενέργειες
            R.drawable.update,
            R.drawable.minimize
        )
    }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Center,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.padding(vertical = 5.dp)
    ) {
        items(
            items = icons,
            key = { it } // ✅ σταθερό state ανά icon
        ) { icon ->

            ActionIconSlot(
                touchSize = touchSize,
                onClick = {
                    when (icon) {
                        R.drawable.update -> viewModel.loadDeviceInfo()
                        R.drawable.ftp -> { /*...*/ }
                        R.drawable.json -> { /*...*/ }
                        R.drawable.ms_word -> { /*...*/ }
                        R.drawable.ms_excel -> { /*...*/ }
                        R.drawable.csv -> { /*...*/ }
                        R.drawable.pdf -> { /*...*/ }
                        R.drawable.png -> { /*...*/ }
                        R.drawable.zip -> { /*...*/ }
                        R.drawable.copy -> { /*...*/ }
                        R.drawable.minimize -> { /*...*/ }
                    }
                }
            ) {
                when (icon) {
                    R.drawable.update -> {
                        var playUpdateGif by rememberSaveable { mutableStateOf(false) }

                        LaunchedEffect(updateInProgress) {
                            if (updateInProgress) playUpdateGif = true
                        }

                        if (!playUpdateGif) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(updateVisualSize)
                            )
                        } else {
                            PlayGifAtLeastWhileInProgress(
                                resId = R.drawable.update_gif,
                                minPlays = 2,
                                inProgress = updateInProgress,
                                modifier = Modifier.size(updateVisualSize),
                                contentDescription = "Refresh",
                                onFinished = {
                                    @Suppress("ASSIGNED_VALUE_IS_NEVER_READ")
                                    playUpdateGif = false
                                }
                            )
                        }
                    }

                    else -> {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun DeviceInfoScreen(
    viewModels: List<AndroidViewModel>
) {
    val timeStart:Long=System.currentTimeMillis()
    val viewModel = remember {
        viewModels.first { it is DeviceInfoViewModel } as DeviceInfoViewModel
    }

    val deviceInfo by viewModel.deviceInfo.collectAsState()   // ✅ εδώ
    val updateInProgress by viewModel.updateInProgress.collectAsState()   // ✅ εδώ

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
            Box(
                modifier = Modifier
                    .padding(
                        //start = DeviceInfoHorizontalPadding,
                        //end = DeviceInfoHorizontalPadding + DeviceInfoScrollbarExtraPadding,
                        top = 6.dp,
                        bottom = 6.dp
                    )
                    .fillMaxWidth()
                    //.background(Color(0xFFF5F7FA))
                    //.border(1.dp, Color(0xFF95959B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    RightSideIcons(
                        viewModel = viewModel,
                        updateInProgress = updateInProgress
                    )
                }

            }



            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mainBottomBarBorderHeight)
                    .background(mainBottomBarBorder)
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                    val sections = remember { buildDeviceInfoSections() }

                    sections.forEach { item ->
                        DeviceInfoSectionItem(
                            item = item,
                            level = 0,
                            deviceInfo = deviceInfo
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }


                    BottomLogos(deviceInfo?.os?.manufacturer ?: "")


                    CpuInfoSpecialThanksRow()   // <- εδώ

                }


                VerticalScrollbar(
                    scrollState = scrollState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp) // ή 0.dp αν τη θες τελείως δεξιά
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        // Wait one frame
        withFrameNanos { }
        val timeEnd = System.currentTimeMillis()
        val duration = timeEnd - timeStart
        Log.d("DeviceInfoScreen","Total load time: $duration")
    }
}

@Composable
fun BottomLogos(manufactor: String) {

    val normalized = manufactor.trim().lowercase()

    val manufactorResource = when (normalized) {
        "", "unknown", "generic", "android", "aosp" -> R.drawable.oem_generic
        "samsung" -> R.drawable.oem_samsung
        "google" -> R.drawable.oem_google
        "lg", "lge" -> R.drawable.oem_lg
        "huawei" -> R.drawable.oem_huawei
        "honor" -> R.drawable.oem_honor
        "xiaomi" -> R.drawable.oem_xiaomi
        "redmi" -> R.drawable.oem_redmi
        "poco" -> R.drawable.oem_poco
        "oppo" -> R.drawable.oem_oppo
        "oneplus" -> R.drawable.oem_oneplus
        "realme" -> R.drawable.oem_realme
        "vivo" -> R.drawable.oem_vivo
        "iqoo" -> R.drawable.oem_iqoo
        "motorola" -> R.drawable.oem_motorola
        "lenovo" -> R.drawable.oem_lenovo
        "sony" -> R.drawable.oem_sony
        "asus" -> R.drawable.oem_asus
        "nokia", "hmd global", "hmd global oy" -> R.drawable.oem_nokia
        "zte" -> R.drawable.oem_zte
        "tcl" -> R.drawable.oem_tcl
        "alcatel" -> R.drawable.oem_alcatel
        "tecno" -> R.drawable.oem_tecno
        "infinix" -> R.drawable.oem_infinix
        "itel" -> R.drawable.oem_itel
        "nothing" -> R.drawable.oem_nothing
        "amazon" -> R.drawable.oem_amazon
        else -> R.drawable.oem_generic
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = manufactorResource),
            contentDescription = "Manufactor logo",
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Mobeetest logo",
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )
        Icon(
            painter = painterResource(id = R.drawable.android),
            contentDescription = "Android logo",
            modifier = Modifier.size(80.dp),
            tint = Color.Unspecified
        )
    }
}



private data class DeviceInfoItem(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val children: List<DeviceInfoItem> = emptyList()
)

private fun buildDeviceInfoSections(): List<DeviceInfoItem> {
    return listOf(
        DeviceInfoItem(
            id = "cpu",
            title = "CPU",
            description = "Information about the main processor (SoC): number of cores, maximum frequencies, supported ABIs and cache levels. This section will show the processing capabilities of the device.",
            iconRes = R.drawable.cpu
        ),
        DeviceInfoItem(
            id = "gpu",
            title = "GPU",
            description = "Information about the the graphics processor: Vulkan / OpenGL ES versions, vendor, renderer and supported extensions. Useful for evaluating graphics capabilities and API support.",
            iconRes = R.drawable.gpu
        ),
        DeviceInfoItem(
            id = "ram",
            title = "RAM",
            description = "Total memory, available memory and usage percentage. Helps you understand how loaded the device is while mobeetest is running.",
            iconRes = R.drawable.ram
        ),
        DeviceInfoItem(
            id = "storage",
            title = "Storage",
            description = "List of all storage volumes (internal, external, SD): total space, used space and file system paths. This shows where mobeetest files are stored on the device.",
            iconRes = R.drawable.storage
        ),
        DeviceInfoItem(
            id = "screen",
            title = "Screen",
            description = "Display information: resolution in pixels, dp dimensions, density class (mdpi/hdpi/...), refresh rate and orientation. Very useful for debugging UI layouts and responsiveness.",
            iconRes = R.drawable.screen
        ),
        DeviceInfoItem(
            id = "os",
            title = "OS",
            description = "Details about the operating system: name (Android / HarmonyOS, etc.), version, SDK level, manufacturer, fingerprint, root status, language, Android ID and security providers.",
            iconRes = R.drawable.android
        ),
        DeviceInfoItem(
            id = "hardware",
            title = "Hardware",
            description = "Overview of the device hardware: battery, cameras, audio, wireless capabilities and USB/OTG. The following subcategories break this down by subsystem.",
            iconRes = R.drawable.hardware,
            children = listOf(
                DeviceInfoItem(
                    id = "hardware_battery",
                    title = "Battery",
                    description = "Battery capacity in mAh and battery technology (Li-ion, Li-Po, etc.). Useful to estimate how long the device can run continuous measurements.",
                    iconRes = R.drawable.battery
                ),
                DeviceInfoItem(
                    id = "hardware_cameras",
                    title = "Cameras",
                    description = "List of device cameras (front, back, external) with sensor orientation. Lets you verify which cameras are available and how they are positioned.",
                    iconRes = R.drawable.camera
                ),
                DeviceInfoItem(
                    id = "hardware_wireless",
                    title = "Wireless",
                    description = "Support for Bluetooth, Bluetooth LE, GPS, NFC, Wi-Fi (5 GHz, Direct, Passpoint), Wi-Fi Aware and IR emitter. Shows the wireless capabilities that can be used in measurement scenarios.",
                    iconRes = R.drawable.wireless
                ),
                DeviceInfoItem(
                    id = "hardware_usb",
                    title = "USB",
                    description = "Information about USB host / OTG support. Useful if you plan to connect external sensors or other devices in future mobeetest features.",
                    iconRes = R.drawable.usb
                ),
                DeviceInfoItem(
                    id = "hardware_sound_cards",
                    title = "Sound cards",
                    description = "Number of logical sound cards / audio output devices reported by the system.",
                    iconRes = R.drawable.sound_card
                )
            )
        ),
        DeviceInfoItem(
            id = "weather",
            title = "Weather",
            description = "Current outdoor weather conditions at the device location (city, temperature, humidity, wind, UV index and visibility). Icons are dynamically adjusted based on the values.",
            iconRes = R.drawable.weather_main   // φτιάξε ένα γενικό weather icon
        ),
        DeviceInfoItem(
            id = "mobeetest",
            title = "Mobeetest",
            description = "Information about the Mobeetest application itself: edition, version, install / update timestamps, push identifiers and runtime statistics.",
            iconRes = R.drawable.ic_launcher_background // placeholder, replace with your own app icon
        )
    )
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
                                info = info.mobeetestInfo,
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

/**
 * Συντομευμένο yes/no για SMS / σημειώσεις.
 */
private fun boolYesNo(value: Boolean): String = if (value) "Yes" else "No"

/**
 * Φτιάχνει ένα ωραίο text block για copy ανά category/subcategory.
 */
private fun buildSectionShareText(
    item: DeviceInfoItem,
    deviceInfo: DeviceInfo?
): String {
    if (deviceInfo == null) {
        return item.title
    }

    return when (item.id) {
        "cpu" -> {
            val cpu = deviceInfo.cpu
            buildString {
                appendLine("CPU")
                appendLine("SoC name: ${cpu.socName}")
                appendLine("Primary ABI: ${cpu.abi}")
                appendLine("Cores: ${cpu.numberOfCores}")
                appendLine("ARM NEON support: ${boolYesNo(cpu.armNeon)}")

                if (cpu.maxFrequenciesMHz.isNotEmpty()) {
                    appendLine("Max frequencies (MHz):")
                    cpu.maxFrequenciesMHz.forEachIndexed { index, mhz ->
                        val text = if (mhz > 0) "$mhz MHz" else "Unknown"
                        appendLine("  • Core $index: $text")
                    }
                }

                if (cpu.l1dCache.isNotEmpty()) {
                    appendLine("L1d cache: ${cpu.l1dCache.joinToString()}")
                }
                if (cpu.l1iCache.isNotEmpty()) {
                    appendLine("L1i cache: ${cpu.l1iCache.joinToString()}")
                }
                if (cpu.l2Cache.isNotEmpty()) {
                    appendLine("L2 cache: ${cpu.l2Cache.joinToString()}")
                }
                if (cpu.l3Cache.isNotEmpty()) {
                    appendLine("L3 cache: ${cpu.l3Cache.joinToString()}")
                }
            }.trim()
        }

        "gpu" -> {
            val gpu = deviceInfo.gpu
            buildString {
                appendLine("GPU")
                appendLine("Vendor: ${gpu.vendor}")
                appendLine("Renderer: ${gpu.renderer}")
                if (gpu.glesVersion.isNotBlank()) {
                    appendLine("OpenGL ES: ${gpu.glesVersion}")
                }
                if (gpu.vulkanVersion.isNotBlank()) {
                    appendLine("Vulkan: ${gpu.vulkanVersion}")
                }
                if (gpu.extensions.isNotEmpty()) {
                    appendLine("Extensions:")
                    gpu.extensions.forEach { ext ->
                        appendLine("  • $ext")
                    }
                }
            }.trim()
        }

        "ram" -> {
            val ram = deviceInfo.ram
            buildString {
                appendLine("RAM")
                appendLine("Total: ${formatMegabytes(ram.totalMB)}")
                appendLine("Available: ${formatMegabytes(ram.availableMB)}")
                appendLine("Low memory threshold: ${formatMegabytes(ram.thresholdMB)}")
                appendLine("Available percent: ${ram.availablePercent.toInt()}%")
            }.trim()
        }

        "storage" -> {
            val storages = deviceInfo.storage
            buildString {
                appendLine("Storage")
                storages.forEach { s ->
                    appendLine()
                    appendLine("─ ${s.label}")
                    appendLine("Path: ${s.path}")
                    appendLine("Total: ${formatBytes(s.totalBytes)}")
                    appendLine("Used: ${formatBytes(s.usedBytes)}")
                    appendLine("Used space: ${s.percentUsed}%")
                }
            }.trim()
        }

        "screen" -> {
            val screen = deviceInfo.screen
            val orientationLabel = when (screen.orientation) {
                0 -> "0 (Portrait)"
                1 -> "90 (Landscape)"
                2 -> "180 (Reverse portrait)"
                3 -> "270 (Reverse landscape)"
                else -> screen.orientation.toString()
            }
            buildString {
                appendLine("Screen")
                appendLine("Screen class: ${screen.screenClass}")
                appendLine("Density class: ${screen.densityClass ?: "Unknown"}")
                appendLine("Visible size (px): ${screen.widthPx} x ${screen.heightPx}")
                appendLine("Visible size (dp): ${screen.dpWidth} x ${screen.dpHeight}")
                appendLine("Absolute size (px): ${screen.absoluteWidthPx} x ${screen.absoluteHeightPx}")
                appendLine("Absolute size (dp): ${screen.absoluteDpWidth} x ${screen.absoluteDpHeight}")
                appendLine("Density: ${String.format(Locale.US, "%.2f", screen.density)}")
                appendLine("Refresh rate: ${String.format(Locale.US, "%.1f Hz", screen.refreshRateHz)}")
                appendLine("Orientation: $orientationLabel")
            }.trim()
        }

        "os" -> {
            val os = deviceInfo.os
            buildString {
                appendLine("OS / Device")
                appendLine("OS name: ${os.osName}")
                appendLine("Version: ${os.version}")
                appendLine("SDK level: ${os.sdk}")
                appendLine("Codename: ${os.codename}")
                appendLine("Manufacturer: ${os.manufacturer}")
                appendLine("Brand: ${os.brand}")
                appendLine("Model: ${os.model}")
                appendLine("Board: ${os.board}")
                appendLine("Kernel: ${os.kernel}")
                appendLine("Rooted: ${boolYesNo(os.isRooted)}")
                appendLine("Running on emulator: ${boolYesNo(os.isEmulator)}")
                appendLine("Encrypted storage: ${os.encryptedStorage}")
                appendLine("StrongBox: ${os.strongBox}")
                os.miuiVersion?.takeIf { it.isNotBlank() }?.let {
                    appendLine("MIUI version: $it")
                }
                appendLine("Language: ${os.language}")
                appendLine("Android ID: ${os.androidId}")
                appendLine("Fingerprint: ${os.fingerprint}")

                if (os.supportedAbis.isNotEmpty()) {
                    appendLine("Supported ABIs:")
                    os.supportedAbis.forEach { abi ->
                        appendLine("  • $abi")
                    }
                }

                if (os.securityProviders.isNotEmpty()) {
                    appendLine("Security providers:")
                    os.securityProviders.forEach { (name, ver) ->
                        appendLine("  • $name: $ver")
                    }
                }

                os.fcmToken?.takeIf { it.isNotBlank() }?.let { token ->
                    appendLine("FCM token: $token")
                }
                os.fcmUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
                    appendLine("FCM UUID: $uuid")
                }
            }.trim()
        }

        "hardware_battery" -> {
            val b = deviceInfo.hardware.battery
            buildString {
                appendLine("Battery")

                val levelStr = b.levelPercent?.let { "$it %" } ?: "Unknown"
                appendLine("Level: $levelStr")

                appendLine("Status: ${describeBatteryStatus(b.status)}")
                appendLine("Power source: ${describeChargerConnection(b.chargerConnection)}")
                appendLine("Health: ${describeBatteryHealth(b.health)}")

                b.temperatureC?.let {
                    appendLine("Temperature: ${String.format(Locale.US, "%.1f °C", it)}")
                }

                val capacityStr = if (b.capacityMah > 0f) {
                    "${b.capacityMah.toInt()} mAh"
                } else {
                    "Unknown"
                }
                appendLine("Capacity: $capacityStr")
                appendLine("Technology: ${b.technology}")
            }.trim()
        }

        "hardware_sound_cards" -> {
            val b = deviceInfo.hardware.soundCardCount
            buildString {
                appendLine("Sound card")
                appendLine("Number of sound cards: $b")
            }.trim()
        }

        "hardware_cameras" -> {
            val cams = deviceInfo.hardware.cameras
            buildString {
                appendLine("Cameras")
                appendLine("Total: ${cams.size}")
                if (cams.isNotEmpty()) {
                    appendLine("Available cameras:")
                    cams.forEachIndexed { index, cam ->
                        appendLine("  • Camera $index: ${cam.type} (${cam.orientation}°)")
                    }
                }
            }.trim()
        }

        "hardware_wireless" -> {
            val w = deviceInfo.hardware.wireless
            buildString {
                appendLine("Wireless")
                appendLine("Bluetooth: ${boolYesNo(w.bluetooth)}")
                appendLine("Bluetooth LE: ${boolYesNo(w.bluetoothLE)}")
                appendLine("GPS: ${boolYesNo(w.gps)}")
                appendLine("NFC: ${boolYesNo(w.nfc)}")
                appendLine("NFC card emulation: ${boolYesNo(w.nfcCardEmulation)}")
                appendLine("Wi-Fi: ${boolYesNo(w.wifi)}")
                appendLine("Wi-Fi Aware: ${boolYesNo(w.wifiAware)}")
                appendLine("Wi-Fi Direct: ${boolYesNo(w.wifiDirect)}")
                appendLine("Wi-Fi Passpoint: ${boolYesNo(w.wifiPasspoint)}")
                appendLine("Wi-Fi 5 GHz band: ${boolYesNo(w.wifi5Ghz)}")
                appendLine("Wi-Fi P2P: ${boolYesNo(w.wifiP2p)}")
                appendLine("IR emitter: ${boolYesNo(w.irEmitter)}")
            }.trim()
        }

        "hardware_usb" -> {
            val usb = deviceInfo.hardware.usb
            buildString {
                appendLine("USB")
                appendLine("USB OTG / Host support: ${boolYesNo(usb.otg)}")
            }.trim()
        }

        // Parent "Hardware" χωρίς δικά του fields → τίτλος + περιγραφή
        "hardware" -> {
            buildString {
                appendLine("Hardware")
                appendLine(item.description)
            }.trim()
        }

        "weather" -> {
            val raw = deviceInfo.weatherInfo
            val map = raw?.let { formatWeatherInfo(it) }.orEmpty()

            buildString {
                appendLine("Weather")
                map.forEach { (k, v) ->
                    appendLine("$k: $v")
                }
            }.trim()
        }

        else -> item.title
    }
}

// -------------------- COMMON FIELD COMPOSABLES --------------------

@Composable
private fun deviceInfoFieldBackground(index: Int): Color {
    // Σαφής zebra: πράσινη/κρεμ εναλλαγή
    return if (index % 2 == 0) {
        Color(0xFFF1F8E9) // even rows - light green
    } else {
        Color(0xFFFFFDE7) // odd rows - light cream
    }
}

private val FieldIndexColumnWidth = 22.dp
private val TableIndexColumnWidth = 40.dp

/**
 * Απλό text field:
 * - αύξων αριθμός
 * - icon ανά field
 * - label αριστερά
 * - value δεξιά
 * - copy icon τέρμα δεξιά
 */
@Composable
fun DeviceInfoValueRow(
    index: Int,
    iconRes: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    infoDescription: String? = null,
    showBottomDivider:Boolean=true
) {
    val clipboardManager = LocalClipboardManager.current
    val bgColor = deviceInfoFieldBackground(index)
    var showInfo by rememberSaveable(label + index.toString() + "_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(label + index.toString() + "_copy") { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .width(FieldIndexColumnWidth),
                textAlign = TextAlign.End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    . size(29.dp)
                    . padding(start = 8.dp, end = 4.dp),
                tint = Color.Unspecified
            )

            val valueText = if (unit.isNullOrBlank()) value else "$value $unit"

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
                modifier = Modifier.weight(1f),
                maxLines = 6,
                overflow = TextOverflow.Clip
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier.size(24.dp).padding(end=2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    clipboardManager.setText(
                        AnnotatedString("$index. $label: $valueText")
                    )
                    showCopied = true
                },
                modifier = Modifier.size(24.dp).padding(end=2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = "Copy $label",
                    tint = Color.Unspecified
                )
            }
        }

        // Copied message
        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, bottom = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.labelSmall,
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
                                shape = RoundedCornerShape(8.dp)
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
        if(showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}


@Composable
fun DeviceInfoBooleanFieldRow(
    index: Int,
    iconRes: Int,
    label: String,
    value: Boolean,
    modifier: Modifier = Modifier,
    infoDescription: String? = null,
    showBottomDivider: Boolean = true
) {
    val clipboardManager = LocalClipboardManager.current
    val bgColor = deviceInfoFieldBackground(index)
    var showInfo by rememberSaveable(label + index.toString() + "_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(label + index.toString() + "_copy") { mutableStateOf(false) }

    val valueText = boolYesNo(value)
    val valueColor = if (value) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .width(FieldIndexColumnWidth),
                textAlign = TextAlign.End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    . size(29.dp)
                    . padding(start = 8.dp, end = 4.dp),
                tint = Color.Unspecified
            )

            Text(
                text = buildAnnotatedString {
                    append(label)
                    append(": ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = valueColor
                        )
                    ) {
                        append(valueText)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier.size(24.dp).padding(end=2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    clipboardManager.setText(
                        AnnotatedString("$index. $label: $valueText")
                    )
                    showCopied = true
                },
                modifier = Modifier.size(24.dp).padding(end=2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = "Copy $label",
                    tint = Color.Unspecified
                )
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
                    style = MaterialTheme.typography.labelSmall,
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
                                shape = RoundedCornerShape(8.dp)
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
        if(showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Πίνακας με μία στήλη value:
 * π.χ. Max frequencies, caches, extensions, supported ABIs, available cameras
 */
@Composable
fun DeviceInfoTextListField(
    index: Int,
    iconRes: Int,
    label: String,
    values: List<String>,
    modifier: Modifier = Modifier,
    maxPreviewItems: Int = 4,
    infoDescription: String? = null,
    showBottomDivider: Boolean = true
) {
    if (values.isEmpty()) {
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = label,
            value = "None",
            modifier = modifier,
            infoDescription = infoDescription
        )
        return
    }

    val clipboardManager = LocalClipboardManager.current
    var expanded by rememberSaveable(label + "_list_expanded") { mutableStateOf(false) }
    var showInfo by rememberSaveable(label + index. toString() + "_list_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(label + index.toString() + "_list_copy") { mutableStateOf(false) }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val total = values.size
    val itemsToShow = if (expanded || total <= maxPreviewItems) {
        values
    } else {
        values. take(maxPreviewItems)
    }
    val bgColor = deviceInfoFieldBackground(index)

    val outlineColor = MaterialTheme.colorScheme.outlineVariant. copy(alpha = 0.6f)
    val headerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    val tableBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val rowEven = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
    val rowOdd = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .bringIntoViewRequester(bringIntoViewRequester)
            .animateContentSize()
    ) {
        // Header row (index, icon, label, info, copy)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme. typography.labelMedium,
                modifier = Modifier
                    .width(FieldIndexColumnWidth),
                textAlign = TextAlign. End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(29.dp)
                    . padding(start = 8.dp, end = 4.dp),
                tint = Color. Unspecified
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier.size(24.dp). padding(end=2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    val copyText = buildString {
                        appendLine("$index. $label")
                        values.forEachIndexed { i, entry ->
                            appendLine("  ${index}.${i + 1}  $entry")
                        }
                    }. trim()
                    clipboardManager. setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier.size(24.dp).padding(end=2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = "Copy $label",
                    tint = Color. Unspecified
                )
            }
        }

        // ✅ Copied message (BEFORE table)
        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, bottom = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ✅ Info tooltip (MOVED HERE - before table)
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
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = infoDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme. colorScheme.surface
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

        // ✅ Table (AFTER info)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                .background(tableBg)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 8.dp, vertical = 4. dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier. width(TableIndexColumnWidth),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Value",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = outlineColor
                )

                itemsToShow.forEachIndexed { idx, entry ->
                    val rowIndexLabel = "${index}.${idx + 1}"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (idx % 2 == 0) rowEven else rowOdd)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment. Top
                    ) {
                        Text(
                            text = rowIndexLabel,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(TableIndexColumnWidth),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = entry,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (idx < itemsToShow. lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.25.dp,
                            color = outlineColor. copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        LaunchedEffect(showCopied) {
            if (showCopied) {
                delay(1_500)
                showCopied = false
            }
        }

        // Show all / Show less με αυτόματο scroll στο "Show less"
        if (total > maxPreviewItems) {
            TextButton(
                onClick = {
                    val newExpanded = !expanded
                    expanded = newExpanded
                    if (! newExpanded) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (expanded) "Show less" else "Show all ($total)",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}


/**
 * Πίνακας key–value:
 * π.χ. Security providers
 */
@Composable
fun DeviceInfoKeyValueListField(
    index: Int,
    iconRes: Int,
    label: String,
    pairs: Map<String, String>,
    modifier: Modifier = Modifier,
    maxPreviewItems: Int = 6,
    infoDescription: String? = null,
    showBottomDivider: Boolean = true
) {
    if (pairs.isEmpty()) {
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = label,
            value = "None",
            modifier = modifier,
            infoDescription = infoDescription
        )
        return
    }

    val clipboardManager = LocalClipboardManager.current
    var expanded by rememberSaveable(label + "_kv_expanded") { mutableStateOf(false) }
    var showInfo by rememberSaveable(label + index. toString() + "_kv_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(label + index.toString() + "_kv_copy") { mutableStateOf(false) }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val entries = pairs.entries.toList()
    val total = entries.size
    val itemsToShow = if (expanded || total <= maxPreviewItems) {
        entries
    } else {
        entries.take(maxPreviewItems)
    }
    val bgColor = deviceInfoFieldBackground(index)

    val outlineColor = MaterialTheme.colorScheme.outlineVariant. copy(alpha = 0.6f)
    val headerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    val tableBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val rowEven = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
    val rowOdd = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .bringIntoViewRequester(bringIntoViewRequester)
            .animateContentSize()
    ) {
        // Header row (index, icon, label, info, copy)
        Row(
            modifier = Modifier
                . fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6. dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme. typography.labelMedium,
                modifier = Modifier
                    .width(FieldIndexColumnWidth),
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

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier. weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier. size(24.dp). padding(end=2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    val copyText = buildString {
                        appendLine("$index.  $label")
                        entries.forEachIndexed { i, (k, v) ->
                            appendLine("  ${index}. ${i + 1}  $k: $v")
                        }
                    }. trim()
                    clipboardManager. setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier.size(24.dp).padding(end=2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = "Copy $label",
                    tint = Color. Unspecified
                )
            }
        }

        // ✅ Copied message (BEFORE table)
        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, bottom = 2.dp),
                contentAlignment = Alignment. CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // ✅ Info tooltip (BEFORE table)
        if (infoDescription != null) {
            AnimatedVisibility(visible = showInfo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12. dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
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

        // ✅ Table (AFTER info tooltip)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                .background(tableBg)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 8.dp, vertical = 4. dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(TableIndexColumnWidth),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = "Value",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier. weight(1.0f),
                        textAlign = TextAlign.End
                    )
                }

                HorizontalDivider(
                    modifier = Modifier. fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = outlineColor
                )

                itemsToShow.forEachIndexed { idx, (k, v) ->
                    val rowIndexLabel = "${index}.${idx + 1}"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (idx % 2 == 0) rowEven else rowOdd)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rowIndexLabel,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(TableIndexColumnWidth),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = k,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.8f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = v,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1.0f),
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (idx < itemsToShow. lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.25.dp,
                            color = outlineColor. copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        LaunchedEffect(showCopied) {
            if (showCopied) {
                delay(1_500)
                showCopied = false
            }
        }

        if (total > maxPreviewItems) {
            TextButton(
                onClick = {
                    val newExpanded = !expanded
                    expanded = newExpanded
                    if (! newExpanded) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (expanded) "Show less" else "Show all ($total)",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}


@Composable
fun PercentageDonut(
    percentage: Float,
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 8.dp,
    label: String = String.format(Locale.US, "%.3f%%", percentage)
) {
    val clamped = percentage.coerceIn(0f, 100f)
    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Base circle - πλήρης κύκλος
            drawArc(
                color = baseColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)  // ✅ Αλλαγή
            )

            // Progress arc - το μπλε μέρος
            val sweep = 360f * (clamped / 100f)
            if (sweep > 0f) {  // Μόνο αν υπάρχει progress
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)  // ✅ Αλλαγή
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DeviceInfoPercentageFieldRow(
    index: Int,
    iconRes: Int,
    label: String,
    percentage: Float,
    modifier: Modifier = Modifier,
    unit: String = "%",
    progressColor: Color?  = null,
    infoDescription: String? = null,
    showBottomDivider:Boolean = true

) {
    val clipboardManager = LocalClipboardManager.current
    val clamped = percentage.coerceIn(0f, 100f)
    val color = progressColor ?: MaterialTheme.colorScheme.primary
    val bgColor = deviceInfoFieldBackground(index)
    var showInfo by rememberSaveable(label + index. toString() + "_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(label + index.toString() + "_copy") { mutableStateOf(false) }

    // 3 δεκαδικά, π. χ. 50. 000%
    val valueText = String.format(Locale.US, "%.3f%s", clamped, unit)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8. dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment. CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme. typography.labelMedium,
                modifier = Modifier
                    .width(FieldIndexColumnWidth),
                textAlign = TextAlign. End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(29.dp)
                    . padding(start = 8.dp, end = 4.dp),
                tint = Color. Unspecified
            )

            // ✅ Text χωρίς weight (wrap_content)
            Text(
                text = buildAnnotatedString {
                    append(label)
                    append(": ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    ) {
                        append(valueText)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ✅ Spacer με weight για να σπρώξει τα υπόλοιπα
            Spacer(modifier = Modifier.weight(1f))

            // ✅ Donut κεντραρισμένο
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .padding(horizontal = 4.dp, vertical = 10.dp)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true),
                contentAlignment = Alignment. Center
            ) {
                PercentageDonut(
                    modifier = Modifier.fillMaxSize(),
                    percentage = clamped,
                    baseColor = MaterialTheme.colorScheme.surfaceVariant,
                    progressColor = color,
                    strokeWidth = 5.dp,
                    label = valueText
                )
            }

            // ✅ Spacer με weight για να κεντράρει το donut
            Spacer(modifier = Modifier.weight(1f))

            // ✅ Info icon
            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier. size(24.dp).padding(end=2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            // Copy icon
            IconButton(
                onClick = {
                    val copyText = "$index. $label: $valueText"
                    clipboardManager. setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier.size(24.dp).padding(end=2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable. copy),
                    contentDescription = "Copy $label",
                    tint = Color.Unspecified
                )
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
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme. primary
                )
            }
        }

        if (infoDescription != null) {
            AnimatedVisibility(visible = showInfo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12. dp, end = 12.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.onSurface. copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8. dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6. dp)
                    ) {
                        Text(
                            text = infoDescription,
                            style = MaterialTheme.typography. bodySmall,
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
        if(showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier
                    . fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant. copy(alpha = 0.4f)
            )
        }
    }
}


fun formatMegabytes(valueMb: Float): String {
    return if (valueMb >= 1024f) {
        val gb = valueMb / 1024f
        String.format(Locale.US, "%.1f GB", gb)
    } else {
        String.format(Locale.US, "%.0f MB", valueMb)
    }
}

fun formatBytes(valueBytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    val tb = gb * 1024

    val v = valueBytes.toDouble()
    return when {
        v >= tb -> String.format(Locale.US, "%.2f TB", v / tb)
        v >= gb -> String.format(Locale.US, "%.2f GB", v / gb)
        v >= mb -> String.format(Locale.US, "%.1f MB", v / mb)
        v >= kb -> String.format(Locale.US, "%.0f KB", v / kb)
        else -> "$valueBytes B"
    }
}

// -------------------- SECTION-SPECIFIC FIELDS --------------------

@Composable
fun CpuSectionFields(cpu: CpuInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "SoC name",
        value = cpu.socName,
        infoDescription = "Human-readable name of the system-on-chip (CPU family) reported by the device."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Primary ABI",
        value = cpu.abi,
        infoDescription = "Main native architecture that apps should target on this device (for example arm64-v8a or x86_64)."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Cores",
        value = cpu.numberOfCores.toString(),
        infoDescription = "Total number of CPU cores exposed to Android."
    )

    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "ARM NEON support",
        value = cpu.armNeon,
        infoDescription = "Indicates whether the CPU supports the ARM NEON SIMD instruction set for accelerated math and media workloads."
    )

    DeviceInfoTextListField(
        index = index++,
        iconRes = iconRes,
        label = "Max frequencies (MHz)",
        values = cpu.maxFrequenciesMHz.mapIndexed { coreIndex, mhz ->
            if (mhz > 0) "Core $coreIndex: $mhz MHz" else "Core $coreIndex: Unknown"
        },
        infoDescription = "Maximum configured clock frequency for each CPU core, in megahertz."
    )

    DeviceInfoTextListField(
        index = index++,
        iconRes = iconRes,
        label = "L1d cache",
        values = cpu.l1dCache,
        infoDescription = "Per-core level 1 data cache sizes reported by the system."
    )
    DeviceInfoTextListField(
        index = index++,
        iconRes = iconRes,
        label = "L1i cache",
        values = cpu.l1iCache,
        infoDescription = "Per-core level 1 instruction cache sizes reported by the system."
    )
    DeviceInfoTextListField(
        index = index++,
        iconRes = iconRes,
        label = "L2 cache",
        values = cpu.l2Cache,
        infoDescription = "Level 2 cache sizes, usually shared by a cluster of CPU cores."
    )
    DeviceInfoTextListField(
        index = index,
        iconRes = iconRes,
        label = "L3 cache",
        values = cpu.l3Cache,
        infoDescription = "Last-level (L3) cache sizes, typically shared across multiple CPU cores.",
        showBottomDivider = false
    )
}

@Composable
fun GpuSectionFields(gpu: GpuInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Vendor",
        value = gpu.vendor,
        infoDescription = "GPU vendor string as reported by the graphics driver."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Renderer",
        value = gpu.renderer,
        infoDescription = "Renderer name that usually includes the GPU model and driver."
    )
    if (gpu.glesVersion.isNotBlank()) {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = "OpenGL ES",
            value = gpu.glesVersion,
            infoDescription = "Highest supported OpenGL ES version for this device."
        )
    }
    if (gpu.vulkanVersion.isNotBlank()) {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = "Vulkan",
            value = gpu.vulkanVersion,
            infoDescription = "Highest supported Vulkan API version for this device."
        )
    }

    DeviceInfoTextListField(
        index = index,
        iconRes = iconRes,
        label = "Extensions",
        values = gpu.extensions,
        maxPreviewItems = 6,
        infoDescription = "List of GPU driver extensions that are available to graphics applications.",
        showBottomDivider = false
    )
}

@Composable
fun RamSectionFields(ram: RamInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Total RAM",
        value = formatMegabytes(ram.totalMB),
        infoDescription = "Approximate total amount of system memory available on the device."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Available RAM",
        value = formatMegabytes(ram.availableMB),
        infoDescription = "Estimated free RAM currently available for apps and the system."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Low memory threshold",
        value = formatMegabytes(ram.thresholdMB),
        infoDescription = "Threshold below which Android considers the device to be in a low-memory state."
    )

    DeviceInfoPercentageFieldRow(
        index = index,
        iconRes = iconRes,
        label = "Available RAM",
        percentage = ram.availablePercent,
        unit = "%",
        progressColor = if (ram.availablePercent < 20f)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary,
        infoDescription = "Percentage of total RAM that is currently available (not in use).",
        showBottomDivider = false
    )
}

@Composable
fun StorageSectionFields(storages: List<StorageInfo>, iconRes: Int) {
    if (storages.isEmpty()) {
        DeviceInfoValueRow(
            index = 1,
            iconRes = iconRes,
            label = "Storage volumes",
            value = "None detected",
            infoDescription = "No storage volumes (internal or external) were reported by the system."
        )
        return
    }


    storages.forEachIndexed { i, storage ->
        StorageVolumeField(
            index = i + 1,
            iconRes = iconRes,
            storage = storage,
            showBottomDivider = (i < storages.size - 1)
        )
    }
}

@Composable
private fun StorageVolumeField(
    index: Int,
    iconRes: Int,
    storage: StorageInfo,
    showBottomDivider: Boolean = true
) {
    val clipboardManager = LocalClipboardManager.current
    val bgColor = deviceInfoFieldBackground(index)

    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val headerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    val tableBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val rowEven = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
    val rowOdd = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)

    val rows = listOf(
        "Path" to storage.path,
        "Total" to formatBytes(storage.totalBytes),
        "Used" to formatBytes(storage.usedBytes),
        "Used space" to "${storage.percentUsed}%"
    )

    var showInfo by rememberSaveable("storage_volume_${storage.label}_$index") { mutableStateOf(false) }
    var showCopied by rememberSaveable("storage_volume_${storage.label}_${index}_copy") { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6. dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(FieldIndexColumnWidth),
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

            Text(
                text = storage.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow. Ellipsis
            )

            IconButton(
                onClick = { showInfo = !showInfo },
                modifier = Modifier.size(24.dp).padding(end=2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.information),
                    contentDescription = "Info ${storage.label}",
                    tint = Color.Unspecified
                )
            }

            IconButton(
                onClick = {
                    val copyText = buildString {
                        appendLine("$index.  ${storage.label}")
                        rows.forEachIndexed { i, (title, value) ->
                            appendLine("  ${index}.${i + 1}  $title: $value")
                        }
                    }. trim()
                    clipboardManager.setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier.size(24.dp).padding(end=2.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = "Copy ${storage. label}",
                    tint = Color.Unspecified
                )
            }
        }

        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, bottom = 2.dp),
                contentAlignment = Alignment. CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(visible = showInfo) {
            Box(
                modifier = Modifier
                    . fillMaxWidth()
                    . padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme. colorScheme.onSurface.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8. dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Storage volume as reported by Android (path, total space, used space and usage percentage).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        // ✅ Table with dynamic column calculation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                .background(tableBg)
        ) {
            DynamicValueColumnTable(
                index = index,
                rows = rows,
                storage = storage,
                headerBg = headerBg,
                rowEven = rowEven,
                rowOdd = rowOdd,
                outlineColor = outlineColor
            )
        }

        if (showInfo) {
            LaunchedEffect(Unit) {
                delay(4_000)
                showInfo = false
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
                modifier = Modifier. fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant. copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun DynamicValueColumnTable(
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
                style = MaterialTheme. typography.labelSmall
            )

            // All row values
            rows.forEach { (title, value) ->
                if (title == "Used space") {
                    Box(
                        modifier = Modifier. padding(horizontal = 4.dp, vertical = 10.dp)
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
        }. map { it.measure(androidx. compose.ui.unit. Constraints()) }

        val maxValueWidthPx = valuePlaceables.maxOfOrNull { it.width } ?: 0
        val numberWidthPx = with(density) { TableIndexColumnWidth.roundToPx() }
        val startPaddingPx = with(density) { 8.dp.roundToPx() }
        val endPaddingPx = with(density) { 8.dp.roundToPx() }  // ✅ Separate end padding

        val availableForTable = constraints.maxWidth - startPaddingPx - endPaddingPx
        val valueColumnWidthPx = maxValueWidthPx. coerceAtMost(availableForTable - numberWidthPx - 100)
        val titleColumnWidthPx = availableForTable - numberWidthPx - valueColumnWidthPx

        // Step 2: Compose actual table with calculated width
        val tablePlaceables = subcompose("actual_table") {
            Column(modifier = Modifier. fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        . fillMaxWidth()
                        . background(headerBg)
                        .padding(start = 8.dp, end = 8.dp, top = 4. dp, bottom = 4.dp),  // ✅ Explicit padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme. typography.labelSmall,
                        modifier = Modifier.width(TableIndexColumnWidth),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.width(with(density) { titleColumnWidthPx.toDp() })
                    )

                    // ✅ Spacer pushes Value column to the right
                    Spacer(modifier = Modifier. weight(1f))

                    Box(
                        modifier = Modifier.width(with(density) { valueColumnWidthPx.toDp() }),
                        contentAlignment = Alignment. Center
                    ) {
                        Text(
                            text = "Value",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier. fillMaxWidth(),
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
                                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),  // ✅ Explicit padding
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rowIndexLabel,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier. width(TableIndexColumnWidth),
                                textAlign = TextAlign. Center
                            )

                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier. width(with(density) { titleColumnWidthPx.toDp() }),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // ✅ Spacer pushes Value column to the right
                            Spacer(modifier = Modifier.weight(1f))

                            Box(
                                modifier = Modifier.width(with(density) { valueColumnWidthPx.toDp() }),
                                contentAlignment = Alignment. Center
                            ) {
                                if (title == "Used space") {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 4. dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier.size(96.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val clamped = storage.percentUsed.toFloat().coerceIn(0f, 100f)
                                            val valueText = String. format(Locale.US, "%.3f%%", clamped)

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

                    if (idx < rows. lastIndex) {
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

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun StorageVolumeFieldPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                . background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            // Preview with short path
            StorageVolumeField(
                index = 1,
                iconRes = R. drawable.storage,
                storage = StorageInfo(
                    label = "Internal Storage",
                    totalBytes = 128L * 1024 * 1024 * 1024, // 128 GB
                    usedBytes = 54L * 1024 * 1024 * 1024,   // 54 GB (42.71%)
                    path = "/data"
                ),
                showBottomDivider = true
            )

            Spacer(modifier = Modifier. height(16.dp))

            // Preview with long external path
            StorageVolumeField(
                index = 2,
                iconRes = R.drawable.storage,
                storage = StorageInfo(
                    label = "SD Card",
                    totalBytes = 256L * 1024 * 1024 * 1024, // 256 GB
                    usedBytes = 192L * 1024 * 1024 * 1024,  // 192 GB (75%)
                    path = "/storage/emulated/0/Android/data/com.example.app/files/external/storage/path/very/long"
                ),
                showBottomDivider = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preview with very high usage
            StorageVolumeField(
                index = 3,
                iconRes = R.drawable. storage,
                storage = StorageInfo(
                    label = "External USB",
                    totalBytes = 64L * 1024 * 1024 * 1024,  // 64 GB
                    usedBytes = 62L * 1024 * 1024 * 1024,   // 62 GB (96.875%)
                    path = "/mnt/usb"
                ),
                showBottomDivider = false
            )
        }
    }
}

@Composable
fun WirelessSectionFields(w: WirelessInfo, iconRes: Int) {
    var index = 1

    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Bluetooth",
        value = w.bluetooth,
        infoDescription = "Indicates whether the device reports support for classic Bluetooth.",
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Bluetooth LE",
        value = w.bluetoothLE,
        infoDescription = "Indicates whether the device supports Bluetooth Low Energy (BLE)."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "GPS",
        value = w.gps,
        infoDescription = "Indicates whether the device has a GPS location provider."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "NFC",
        value = w.nfc,
        infoDescription = "Indicates whether the device supports Near Field Communication."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "NFC card emulation",
        value = w.nfcCardEmulation,
        infoDescription = "Whether the NFC hardware can emulate cards for secure transactions or access control."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi",
        value = w.wifi,
        infoDescription = "Indicates whether the device has Wi-Fi networking capability."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi Aware",
        value = w.wifiAware,
        infoDescription = "Support for Wi-Fi Aware (neighbor awareness networking) APIs."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi Direct",
        value = w.wifiDirect,
        infoDescription = "Support for Wi-Fi Direct peer-to-peer connections."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi Passpoint",
        value = w.wifiPasspoint,
        infoDescription = "Support for Wi-Fi Passpoint / Hotspot 2.0 automatic hotspot roaming."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi 5 GHz band",
        value = w.wifi5Ghz,
        infoDescription = "Whether the Wi-Fi radio can use the 5 GHz band."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Wi-Fi P2P",
        value = w.wifiP2p,
        infoDescription = "Whether the device reports support for Wi-Fi peer-to-peer APIs."
    )
    DeviceInfoBooleanFieldRow(
        index = index,
        iconRes = iconRes,
        label = "IR emitter",
        value = w.irEmitter,
        infoDescription = "Indicates if the device has an infrared (IR) emitter for remote-control use cases.",
        showBottomDivider = false
    )
}

private fun describeBatteryHealth(health: BatteryHealth?): String =
    when (health) {
        BatteryHealth.GOOD -> "Good"
        BatteryHealth.FAILED -> "Failure"
        BatteryHealth.DEAD -> "Dead"
        BatteryHealth.OVERVOLTAGE -> "Over voltage"
        BatteryHealth.OVERHEATED -> "Overheated"
        BatteryHealth.UNKNOWN, null -> "Unknown"
    }

private fun describeChargerConnection(conn: ChargerConnection?): String =
    when (conn) {
        ChargerConnection.AC -> "AC charger"
        ChargerConnection.USB -> "USB"
        ChargerConnection.WIRELESS -> "Wireless"
        ChargerConnection.NONE -> "On battery"
        ChargerConnection.UNKNOWN, null -> "Unknown"
    }

private fun describeBatteryStatus(status: BatteryStatus?): String =
    when (status) {
        BatteryStatus.CHARGING -> "Charging"
        BatteryStatus.DISCHARGING -> "Discharging"
        BatteryStatus.NOT_CHARGING -> "Not charging"
        BatteryStatus.FULL -> "Full"
        BatteryStatus.UNKNOWN, null -> "Unknown"
    }

@Composable
fun BatterySectionFields(
    b: BatteryInfo,
    iconRes: Int
) {
    var index = 1

    // 1. Battery level with donut
    val levelPercent = b.levelPercent?.coerceIn(0, 100)
    if (levelPercent != null) {
        val levelF = levelPercent.toFloat()
        val color = when {
            levelF < 15f -> MaterialTheme.colorScheme.error
            levelF < 30f -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        }

        DeviceInfoPercentageFieldRow(
            index = index++,
            iconRes = iconRes,
            label = "Battery level",
            percentage = levelF,
            progressColor = color,
            infoDescription = "Current battery charge level as a percentage of full capacity."
        )
    } else {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = "Battery level",
            value = "Unknown",
            infoDescription = "Current battery charge level as a percentage of full capacity."
        )
    }

    // 2. Status
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Status",
        value = describeBatteryStatus(b.status),
        infoDescription = "Charging state of the battery as reported by Android."
    )

    // 3. Power source
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Power source",
        value = describeChargerConnection(b.chargerConnection),
        infoDescription = "Current power source: AC, USB, wireless charger or running on battery."
    )

    // 4. Health
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Health",
        value = describeBatteryHealth(b.health),
        infoDescription = "Battery health reported by the system (good, overheated, dead, etc.)."
    )

    // 5. Temperature
    val tempText = b.temperatureC?.let { String.format(Locale.US, "%.1f", it) } ?: "Unknown"
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Temperature",
        value = tempText,
        unit = if (b.temperatureC != null) "°C" else null,
        infoDescription = "Current battery temperature in degrees Celsius."
    )

    // 6. Capacity
    val capacityText = if (b.capacityMah > 0f) b.capacityMah.toInt().toString() else "Unknown"
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Battery capacity",
        value = capacityText,
        unit = if (b.capacityMah > 0f) "mAh" else null,
        infoDescription = "Approximate nominal battery capacity in milliampere-hours as reported by the system."
    )

    // 7. Technology (last row)
    DeviceInfoValueRow(
        index = index,
        iconRes = iconRes,
        label = "Technology",
        value = b.technology,
        infoDescription = "Battery chemistry string, for example Li-ion or Li-polymer.",
        showBottomDivider = false
    )
}

@Composable
fun ScreenSectionFields(screen: ScreenInfo, iconRes: Int) {
    var index = 1

    val orientationLabel = when (screen.orientation) {
        0 -> "0 (Portrait)"
        1 -> "90 (Landscape)"
        2 -> "180 (Reverse portrait)"
        3 -> "270 (Reverse landscape)"
        else -> screen.orientation.toString()
    }

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Screen class",
        value = screen.screenClass,
        infoDescription = "Logical size bucket of the screen (for example small, normal, large)."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Density class",
        value = screen.densityClass ?: "Unknown",
        infoDescription = "Density bucket used by Android resources (mdpi, hdpi, xhdpi, etc.)."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Visible size (px)",
        value = "${screen.widthPx} x ${screen.heightPx}",
        infoDescription = "Current visible size of the window in raw pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Visible size (dp)",
        value = "${screen.dpWidth} x ${screen.dpHeight}",
        infoDescription = "Current visible size of the window in density-independent pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Absolute size (px)",
        value = "${screen.absoluteWidthPx} x ${screen.absoluteHeightPx}",
        infoDescription = "Absolute physical display resolution in raw pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Absolute size (dp)",
        value = "${screen.absoluteDpWidth} x ${screen.absoluteDpHeight}",
        infoDescription = "Absolute physical display resolution converted to density-independent pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Density",
        value = String.format(Locale.US, "%.2f", screen.density),
        infoDescription = "Logical density of the display used to scale dp units to pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Refresh rate",
        value = String.format(Locale.US, "%.1f Hz", screen.refreshRateHz),
        infoDescription = "Approximate display refresh rate in hertz."
    )

    DeviceInfoValueRow(
        index = index,
        iconRes = iconRes,
        label = "Orientation",
        value = orientationLabel,
        infoDescription = "Current screen orientation in degrees relative to the natural orientation.",
        showBottomDivider = false
    )
}

@Composable
fun OsSectionFields(os: OsInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "OS name",
        value = os.osName,
        infoDescription = "Name of the operating system reported by the device (for example Android)."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Version",
        value = os.version,
        infoDescription = "Human-readable OS version string shown to the user."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "SDK level",
        value = os.sdk.toString(),
        infoDescription = "Android API level (SDK_INT) used by apps to check feature availability."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Codename",
        value = os.codename,
        infoDescription = "Internal Android codename associated with this release."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Manufacturer",
        value = os.manufacturer,
        infoDescription = "Device manufacturer as reported by Build.MANUFACTURER."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Brand",
        value = os.brand,
        infoDescription = "High-level brand name (for example Google, Samsung, Xiaomi)."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Model",
        value = os.model,
        infoDescription = "Marketing model string shown to users and apps."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Board",
        value = os.board,
        infoDescription = "Low-level board or hardware platform identifier."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Kernel",
        value = os.kernel,
        infoDescription = "Linux kernel version and build information."
    )

    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Rooted",
        value = os.isRooted,
        infoDescription = "Best-effort check indicating whether the device appears to be rooted."
    )
    DeviceInfoBooleanFieldRow(
        index = index++,
        iconRes = iconRes,
        label = "Running on emulator",
        value = os.isEmulator,
        infoDescription = "Best-effort check indicating whether this device seems to be an emulator."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Encrypted storage",
        value = os.encryptedStorage,
        infoDescription = "Describes whether the primary storage is reported as encrypted."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "StrongBox",
        value = os.strongBox,
        infoDescription = "Indicates presence of a StrongBox-backed hardware security module when available."
    )

    os.miuiVersion?.takeIf { it.isNotBlank() }?.let {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = "MIUI version",
            value = it,
            infoDescription = "MIUI firmware version string for Xiaomi devices."
        )
    }

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Language",
        value = os.language,
        infoDescription = "Current primary system language (ISO code)."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Android ID",
        value = os.androidId,
        infoDescription = "Stable Android ID string scoped to this device and user profile."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Fingerprint",
        value = os.fingerprint,
        infoDescription = "Build fingerprint uniquely identifying this OS build and device configuration."
    )

    if (os.supportedAbis.isNotEmpty()) {
        DeviceInfoTextListField(
            index = index++,
            iconRes = iconRes,
            label = "Supported ABIs",
            values = os.supportedAbis,
            infoDescription = "Ordered list of all native ABIs supported by the runtime on this device."
        )
    }

    if (os.securityProviders.isNotEmpty()) {
        DeviceInfoKeyValueListField(
            index = index++,
            iconRes = iconRes,
            label = "Security providers",
            pairs = os.securityProviders,
            infoDescription = "Installed Java security providers and their versions, as seen by the platform.",
            showBottomDivider = (os.fcmToken!= null && os.fcmUuid != null)
        )
    }

    os.fcmToken?.takeIf { it.isNotBlank() }?.let { token ->
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = "FCM token",
            value = token,
            infoDescription = "Current Firebase Cloud Messaging registration token used to reach this device.",
            showBottomDivider = (os.fcmUuid != null)
        )
    }

    os.fcmUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = "FCM UUID",
            value = uuid,
            infoDescription = "Internal UUID used by the app to associate the device with backend records.",
            showBottomDivider = false
        )
    }
}

@Composable
fun CamerasSectionFields(cameras: List<CameraInfo>, iconRes: Int) {
    var index = 1

    if (cameras.isEmpty()) {
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = "Cameras",
            value = "None detected",
            infoDescription = "No camera devices were reported by the system."
        )
        return
    }

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Total cameras",
        value = cameras.size.toString(),
        infoDescription = "Total number of camera devices reported by Android (front, back or external)."
    )

    val entries = cameras.mapIndexed { camIndex, cam ->
        "Camera $camIndex: ${cam.type} (${cam.orientation}°)"
    }

    DeviceInfoTextListField(
        index = index,
        iconRes = iconRes,
        label = "Available cameras",
        values = entries,
        maxPreviewItems = 4,
        showBottomDivider = false,
        infoDescription = "List of all available cameras with their type and sensor orientation."
    )
}

@Composable
fun UsbSectionFields(usb: UsbInfo, iconRes: Int) {
    val index = 1

    DeviceInfoBooleanFieldRow(
        index = index,
        iconRes = iconRes,
        label = "USB OTG / Host support",
        value = usb.otg,
        infoDescription = "Indicates whether the device can act as a USB host (OTG) for external peripherals.",
        showBottomDivider = false
    )
}


@Composable
fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp
) {
    val colorScheme = MaterialTheme.colorScheme
    val trackColor = colorScheme.onSurface.copy(alpha = 0.08f)
    val thumbColor = colorScheme.primary.copy(alpha = 0.9f)

    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(thickness)
    ) {
        // Track
        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(
                x = size.width / 2f,
                y = size.width / 2f
            )
        )

        if (scrollState.maxValue > 0) {
            // Content & visible fraction
            val contentHeight = scrollState.maxValue.toFloat() + size.height
            val visibleFraction = size.height / contentHeight

            // Dynamic thumb height based on how much περιεχόμενο χωράει στην οθόνη
            val thumbHeight = (size.height * visibleFraction)
                .coerceAtLeast(size.height * 0.05f) // optional ελάχιστο 5%

            val maxOffset = size.height - thumbHeight

            val fraction = (scrollState.value.toFloat() / scrollState.maxValue.toFloat())
                .coerceIn(0f, 1f)

            val thumbOffsetY = maxOffset * fraction

            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(0f, thumbOffsetY),
                size = Size(size.width, thumbHeight),
                cornerRadius = CornerRadius(
                    x = size.width / 2f,
                    y = size.width / 2f
                )
            )
        }
    }
}


@Composable
fun SoundCardsSectionFields(
    soundCardCount: Int,
    iconRes: Int
) {
    DeviceInfoValueRow(
        index = 1,
        iconRes = iconRes,
        label = "Sound card count",
        value = soundCardCount.toString(),
        infoDescription = "Total number of logical sound cards / audio output devices reported by Android on this device.",
        showBottomDivider = false
    )
}
private fun countFieldsForSection(
    itemId: String,
    deviceInfo: DeviceInfo?
): Int {
    if (deviceInfo == null) return 0

    return when (itemId) {
        "cpu" -> 9

        "gpu" -> {
            val gpu = deviceInfo.gpu
            var count = 2 // Vendor, Renderer
            if (gpu.glesVersion.isNotBlank()) count++
            if (gpu.vulkanVersion.isNotBlank()) count++
            count++ // Extensions list
            count
        }

        "ram" -> 4

        "storage" -> {
            val storages = deviceInfo.storage
            if (storages.isEmpty()) 1 else storages.size
        }

        "screen" -> 9

        "os" -> {
            val os = deviceInfo.os
            var count = 0

            // Βασικά text fields
            count++ // OS name
            count++ // Version
            count++ // SDK level
            count++ // Codename
            count++ // Manufacturer
            count++ // Brand
            count++ // Model
            count++ // Board
            count++ // Kernel

            // Booleans
            count++ // Rooted
            count++ // Running on emulator

            // Ακόμα text fields
            count++ // Encrypted storage
            count++ // StrongBox

            if (!os.miuiVersion.isNullOrBlank()) {
                count++ // MIUI version
            }

            count++ // Language
            count++ // Android ID
            count++ // Fingerprint

            if (os.supportedAbis.isNotEmpty()) count++
            if (os.securityProviders.isNotEmpty()) count++
            if (!os.fcmToken.isNullOrBlank()) count++
            if (!os.fcmUuid.isNullOrBlank()) count++

            count
        }

        "hardware_battery" -> 7

        "hardware_cameras" -> {
            val cams = deviceInfo.hardware.cameras
            if (cams.isEmpty()) 1 else 2
        }

        "hardware_wireless" -> 12

        "hardware_usb" -> 1
        "hardware_sound_cards" -> 1
        "weather" -> 11
        "mobeetest" -> {
            val m = deviceInfo.mobeetestInfo
            buildMobeetestRows(m).size +
                    (if (m.ramPercentageUsage != null) 1 else 0) +
                    (if (m.cpuPercentageUsage != null) 1 else 0)
        }
        else -> 0
    }
}

fun formatWeatherInfo(raw: String): Map<String, String> {
    return try {
        val weatherJson = JSONObject(raw)
        val location = weatherJson.getJSONObject("location")
        val current = weatherJson.getJSONObject("current")
        val condition = current.getJSONObject("condition")

        mapOf(
            "City" to location.optString("name"),
            "Region" to location.optString("region"),
            "Country" to location.optString("country"),
            "Local time" to location.optString("localtime"),
            "Temperature" to "${current.optDouble("temp_c")} °C",
            "Condition" to condition.optString("text"),
            "Feels like" to "${current.optDouble("feelslike_c")} °C",
            "Wind" to "${current.optDouble("wind_kph")} kph (${current.optString("wind_dir")})",
            "Humidity" to "${current.optInt("humidity")} %",
            "UV index" to current.optDouble("uv").toString(),
            "Visibility" to "${current.optDouble("vis_km")} km"
        )
    } catch (e: Exception) {
        mapOf("Weather info" to "Invalid JSON: ${e.localizedMessage}")
    }
}


data class WeatherParsedInfo(
    val city: String?,
    val region: String?,
    val country: String?,
    val localTime: String?,
    val temperatureC: Double?,
    val conditionText: String?,
    val isDay: Boolean?,
    val feelsLikeC: Double?,
    val windKph: Double?,
    val windDir: String?,
    val humidity: Int?,
    val uvIndex: Double?,
    val visibilityKm: Double?
)

private fun parseWeatherInfoJson(raw: String?): WeatherParsedInfo? {
    val jsonString = raw?.takeIf { it.isNotBlank() } ?: return null

    return try {
        val json = JSONObject(jsonString)
        val location = json.getJSONObject("location")
        val current = json.getJSONObject("current")
        val condition = current.getJSONObject("condition")

        val isDayRaw = current.optInt("is_day", -1)
        val isDay = when (isDayRaw) {
            0 -> false
            1 -> true
            else -> null
        }

        val city = location.optString("name")
            .takeIf { it.isNotBlank() }

        val region = location.optString("region")
            .takeIf { it.isNotBlank() }

        val country = location.optString("country")
            .takeIf { it.isNotBlank() }

        val localTime = location.optString("localtime")
            .takeIf { it.isNotBlank() }

        val tempRaw = current.optDouble("temp_c", Double.NaN)
        val temperatureC = tempRaw.takeUnless { it.isNaN() }

        val feelsRaw = current.optDouble("feelslike_c", Double.NaN)
        val feelsLikeC = feelsRaw.takeUnless { it.isNaN() }

        val windKphRaw = current.optDouble("wind_kph", Double.NaN)
        val windKph = windKphRaw.takeUnless { it.isNaN() }

        val windDir = current.optString("wind_dir")
            .takeIf { it.isNotBlank() }

        val humidityRaw = current.optInt("humidity", -1)
        val humidity = humidityRaw.takeUnless { it < 0 }

        val uvRaw = current.optDouble("uv", Double.NaN)
        val uvIndex = uvRaw.takeUnless { it.isNaN() }

        val visRaw = current.optDouble("vis_km", Double.NaN)
        val visibilityKm = visRaw.takeUnless { it.isNaN() }

        val conditionText = condition.optString("text")
            .takeIf { it.isNotBlank() }

        WeatherParsedInfo(
            city = city,
            region = region,
            country = country,
            localTime = localTime,
            temperatureC = temperatureC,
            conditionText = conditionText,
            isDay = isDay,
            feelsLikeC = feelsLikeC,
            windKph = windKph,
            windDir = windDir,
            humidity = humidity,
            uvIndex = uvIndex,
            visibilityKm = visibilityKm
        )
    } catch (_: Exception) {
        null
    }
}



private enum class WeatherConditionCategory {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    RAIN,
    SNOW,
    STORM
}

private fun categorizeWeatherCondition(
    text: String?,
    isDay: Boolean?
): WeatherConditionCategory {
    val t = text?.lowercase(Locale.US).orEmpty()

    return when {
        "thunder" in t || "storm" in t -> WeatherConditionCategory.STORM
        "snow" in t || "sleet" in t || "blizzard" in t || "ice pellet" in t ->
            WeatherConditionCategory.SNOW
        "rain" in t || "drizzle" in t || "shower" in t ->
            WeatherConditionCategory.RAIN
        "fog" in t || "mist" in t || "haze" in t ->
            WeatherConditionCategory.FOG
        "overcast" in t || "cloudy" in t ->
            WeatherConditionCategory.CLOUDY
        "partly" in t || "patchy" in t ->
            WeatherConditionCategory.PARTLY_CLOUDY
        "sunny" in t || "clear" in t -> {
            if (isDay == false) WeatherConditionCategory.CLEAR_NIGHT
            else WeatherConditionCategory.CLEAR_DAY
        }
        else -> {
            if (isDay == false) WeatherConditionCategory.CLEAR_NIGHT
            else WeatherConditionCategory.CLEAR_DAY
        }
    }
}

@DrawableRes
private fun iconForCondition(
    text: String?,
    isDay: Boolean?
): Int {
    return when (categorizeWeatherCondition(text, isDay)) {
        WeatherConditionCategory.CLEAR_DAY -> R.drawable.weather_condition_clear_day
        WeatherConditionCategory.CLEAR_NIGHT -> R.drawable.weather_condition_clear_night
        WeatherConditionCategory.PARTLY_CLOUDY -> R.drawable.weather_condition_partly_cloudy
        WeatherConditionCategory.CLOUDY -> R.drawable.weather_condition_cloudy
        WeatherConditionCategory.FOG -> R.drawable.weather_condition_fog
        WeatherConditionCategory.RAIN -> R.drawable.weather_condition_rain
        WeatherConditionCategory.SNOW -> R.drawable.weather_condition_snow
        WeatherConditionCategory.STORM -> R.drawable.weather_condition_storm
    }
}

// ---------------- TEMPERATURE ----------------

private enum class TemperatureBucket {
    FREEZING,
    COLD,
    MILD,
    HOT
}

private fun temperatureBucket(tempC: Double?): TemperatureBucket {
    val t = tempC ?: return TemperatureBucket.MILD
    return when {
        t <= 0.0 -> TemperatureBucket.FREEZING
        t <= 15.0 -> TemperatureBucket.COLD
        t <= 28.0 -> TemperatureBucket.MILD
        else -> TemperatureBucket.HOT
    }
}

@DrawableRes
private fun iconForTemperature(tempC: Double?): Int {
    return when (temperatureBucket(tempC)) {
        TemperatureBucket.FREEZING -> R.drawable.weather_temp_freezing
        TemperatureBucket.COLD -> R.drawable.weather_temp_cold
        TemperatureBucket.MILD -> R.drawable.weather_temp_mild
        TemperatureBucket.HOT -> R.drawable.weather_temp_hot
    }
}

// ---------------- UV INDEX ----------------

private enum class UvBucket {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH,
    EXTREME
}

private fun uvBucket(uv: Double?): UvBucket {
    val v = uv ?: return UvBucket.LOW
    return when {
        v < 3.0 -> UvBucket.LOW
        v < 6.0 -> UvBucket.MODERATE
        v < 8.0 -> UvBucket.HIGH
        v < 11.0 -> UvBucket.VERY_HIGH
        else -> UvBucket.EXTREME
    }
}

@DrawableRes
private fun iconForUvIndex(uv: Double?): Int {
    return when (uvBucket(uv)) {
        UvBucket.LOW -> R.drawable.weather_uv_low
        UvBucket.MODERATE -> R.drawable.weather_uv_moderate
        UvBucket.HIGH -> R.drawable.weather_uv_high
        UvBucket.VERY_HIGH -> R.drawable.weather_uv_very_high
        UvBucket.EXTREME -> R.drawable.weather_uv_extreme
    }
}


@Composable
fun WeatherSectionFields(
    weatherJson: String?,
    iconRes: Int
) {
    val parsed = remember(weatherJson) { parseWeatherInfoJson(weatherJson) }
    val map = remember(weatherJson) { weatherJson?.let { formatWeatherInfo(it) } }

    var index = 1

    if (parsed == null || map == null || map.isEmpty()) {
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = "Weather",
            value = "Not available",
            infoDescription = "The app could not retrieve weather information for this device location (location permission or network may be disabled).",
            showBottomDivider = false
        )
        return
    }

    // 1. City
    DeviceInfoValueRow(
        index = index++,
        iconRes = R.drawable.weather_city,
        label = "City",
        value = map["City"]. orEmpty(),
        infoDescription = "City name resolved from the device coordinates."
    )

    // 2. Region
    DeviceInfoValueRow(
        index = index++,
        iconRes = R.drawable. weather_region,
        label = "Region",
        value = map["Region"].orEmpty(),
        infoDescription = "Administrative region (state / prefecture) for the location."
    )

    // 3. Country
    DeviceInfoValueRow(
        index = index++,
        iconRes = R.drawable.weather_country,
        label = "Country",
        value = map["Country"].orEmpty(),
        infoDescription = "Country name as reported by the weather provider."
    )

    val localTimeMillis = parsed.localTime?.let { timeStr ->
        try {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .parse(timeStr)?. time
        } catch (_: Exception) {
            null
        }
    }

    // 4. Local time
    if (localTimeMillis != null) {
        DeviceInfoDateTimeFieldRow(
            index = index++,
            iconRes = R.drawable.weather_time,
            label = "Local time",
            millis = localTimeMillis,
            valueOverride = formatDateTimeNoSeconds(localTimeMillis),
            infoDescription = "Local date and time in the reported city."
        )
    } else {
        DeviceInfoValueRow(
            index = index++,
            iconRes = R. drawable.weather_time,
            label = "Local time",
            value = map["Local time"].orEmpty(),
            infoDescription = "Local date and time in the reported city."
        )
    }

    // 5. Condition (dynamic icon)
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconForCondition(parsed.conditionText, parsed.isDay),
        label = "Condition",
        value = map["Condition"].orEmpty(),
        infoDescription = "Short human-readable description of the current weather condition (sunny, cloudy, rain, storm, etc.)."
    )

    // ✅ 6-9: Aligned fields (Temperature, Feels like, Wind, Humidity)
    WeatherAlignedFieldsGroup(
        startIndex = index,
        parsed = parsed,
        map = map
    )
    index += 4

    // 10. UV index (dynamic icon)
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconForUvIndex(parsed.uvIndex),
        label = "UV index",
        value = map["UV index"].orEmpty(),
        infoDescription = "Ultraviolet index level indicating potential skin damage risk."
    )

    // 11. Visibility (static icon, last row)
    DeviceInfoValueRow(
        index = index,
        iconRes = R.drawable.weather_visibility,
        label = "Visibility",
        value = map["Visibility"].orEmpty(),
        infoDescription = "Estimated horizontal visibility distance in kilometers.",
        showBottomDivider = false
    )
}

@Composable
private fun WeatherAlignedFieldsGroup(
    startIndex: Int,
    parsed: WeatherParsedInfo,
    map: Map<String, String>
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // ✅ Lightweight text measurement (no full composition)
    val dimensions = remember(
        parsed.temperatureC,
        parsed.feelsLikeC,
        parsed.windKph,
        parsed.windDir,
        parsed.humidity,
        textMeasurer
    ) {
        val textStyle = TextStyle(
            fontSize = 16.sp,  // MaterialTheme.typography.bodyMedium default
            fontWeight = FontWeight. Normal
        )

        // Measure all 4 text strings
        val texts = listOf(
            buildAnnotatedString {
                append("Temperature: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(String.format(Locale.US, "%.1f °C", parsed.temperatureC ?: 0.0))
                }
            },
            buildAnnotatedString {
                append("Feels like: ")
                withStyle(SpanStyle(fontWeight = FontWeight. Bold)) {
                    append(String.format(Locale.US, "%.1f °C", parsed.feelsLikeC ?: 0.0))
                }
            },
            buildAnnotatedString {
                val windDir = parsed.windDir?. takeIf { it. isNotBlank() } ?: "?"
                val windValue = String.format(Locale.US, "%.1f kph", parsed.windKph ?: 0.0)
                append("Wind: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$windValue ($windDir)")
                }
            },
            buildAnnotatedString {
                append("Humidity: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(String.format(Locale.US, "%.3f%%", (parsed.humidity ?: 0). toFloat()))
                }
            }
        )

        // Measure max width
        val maxWidthPx = texts.maxOf { text ->
            textMeasurer. measure(
                text = text,
                style = textStyle,
                maxLines = 1
            ).size.width
        }

        val textWidth = with(density) { maxWidthPx.toDp() }
        val visualWidth = 96.dp  // Fixed

        Pair(textWidth, visualWidth)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 6.  Temperature
        parsed.temperatureC?.let { temp ->
            WeatherAlignedTemperatureRow(
                index = startIndex,
                iconRes = iconForTemperature(temp),
                label = "Temperature",
                temperatureC = temp,
                textWidth = dimensions.first,
                visualWidth = dimensions.second,
                infoDescription = "Current air temperature near the surface."
            )
        } ?: run {
            DeviceInfoValueRow(
                index = startIndex,
                iconRes = iconForTemperature(null),
                label = "Temperature",
                value = map["Temperature"]. orEmpty(),
                infoDescription = "Current air temperature near the surface."
            )
        }

        // 7. Feels like
        parsed.feelsLikeC?.let { feels ->
            WeatherAlignedTemperatureRow(
                index = startIndex + 1,
                iconRes = iconForTemperature(feels),
                label = "Feels like",
                temperatureC = feels,
                textWidth = dimensions.first,
                visualWidth = dimensions.second,
                infoDescription = "Perceived temperature taking into account wind and humidity."
            )
        } ?: run {
            DeviceInfoValueRow(
                index = startIndex + 1,
                iconRes = iconForTemperature(null),
                label = "Feels like",
                value = map["Feels like"].orEmpty(),
                infoDescription = "Perceived temperature taking into account wind and humidity."
            )
        }

        // 8. Wind
        parsed.windKph?. let { wind ->
            WeatherAlignedWindRow(
                index = startIndex + 2,
                iconRes = R.drawable. weather_wind,
                label = "Wind",
                windKph = wind,
                windDir = parsed.windDir,
                textWidth = dimensions.first,
                visualWidth = dimensions.second,
                infoDescription = "Wind speed in kilometers per hour and main wind direction."
            )
        } ?: run {
            DeviceInfoValueRow(
                index = startIndex + 2,
                iconRes = R.drawable.weather_wind,
                label = "Wind",
                value = map["Wind"].orEmpty(),
                infoDescription = "Wind speed in kilometers per hour and main wind direction."
            )
        }

        // 9. Humidity
        parsed. humidity?.let { hum ->
            WeatherAlignedHumidityRow(
                index = startIndex + 3,
                iconRes = R.drawable.weather_humidity,
                label = "Humidity",
                percentage = hum. toFloat(),
                textWidth = dimensions.first,
                visualWidth = dimensions.second,
                infoDescription = "Relative humidity of the air, expressed as a percentage."
            )
        } ?: run {
            DeviceInfoValueRow(
                index = startIndex + 3,
                iconRes = R.drawable.weather_humidity,
                label = "Humidity",
                value = map["Humidity"].orEmpty(),
                infoDescription = "Relative humidity of the air, expressed as a percentage."
            )
        }
    }
}

@Composable
private fun WeatherAlignedTemperatureRow(
    index: Int,
    iconRes: Int,
    label: String,
    temperatureC: Double,
    textWidth: Dp,
    visualWidth: Dp,
    infoDescription: String?
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
                .padding(start = 8. dp, end = 12.dp, top = 8.dp, bottom = 8. dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier. width(FieldIndexColumnWidth),
                textAlign = TextAlign.End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(29.dp)
                    .padding(start = 8.dp, end = 4.dp),
                tint = Color. Unspecified
            )

            Box(
                modifier = Modifier. width(textWidth),
                contentAlignment = Alignment. CenterStart
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(label)
                        append(": ")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight. Bold,
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

            Spacer(modifier = Modifier. weight(1f))

            Box(
                modifier = Modifier
                    .width(visualWidth)
                    .height(64.dp),
                contentAlignment = Alignment. Center
            ) {
                Box(
                    modifier = Modifier. size(width = 26.dp, height = 64.dp)
                ) {
                    ThermometerMini(
                        tempC = temperatureC,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier. weight(1f))

            // ✅ Icons με ZERO padding
            // ✅ Icons in ROW με ΣΩΣΤΟ spacing όπως τις άλλες γραμμές
            Row(
                horizontalArrangement = Arrangement. spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (infoDescription != null) {
                    IconButton(
                        onClick = { showInfo = !showInfo },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 2.dp)  // ✅ Όπως πριν
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.information),
                            contentDescription = "Info $label",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)  // ✅ FULL size
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
                        .padding(start = 2.dp)  // ✅ Όπως πριν
                ) {
                    Icon(
                        painter = painterResource(R.drawable.copy),
                        contentDescription = "Copy $label",
                        tint = Color.Unspecified
                    )
                }
            }
        }

        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12. dp, bottom = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme. typography.labelSmall,
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
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = infoDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme. colorScheme.surface
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
            modifier = Modifier. fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant. copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun WeatherAlignedWindRow(
    index: Int,
    iconRes: Int,
    label: String,
    windKph: Double,
    windDir: String?,
    textWidth: Dp,
    visualWidth: Dp,
    infoDescription: String?
) {
    val clipboardManager = LocalClipboardManager.current
    val bgColor = deviceInfoFieldBackground(index)

    var showInfo by rememberSaveable("${label}_${index}_wind_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable("${label}_${index}_wind_copy") { mutableStateOf(false) }

    val dirText = windDir?.takeIf { it.isNotBlank() } ?: "?"
    val windValue = String.format(Locale.US, "%.1f kph", windKph)
    val valueText = "$windValue ($dirText)"

    val degrees = windDirToDegrees(windDir)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                . padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier. width(FieldIndexColumnWidth),
                textAlign = TextAlign. End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(29.dp)
                    . padding(start = 8.dp, end = 4.dp),
                tint = Color. Unspecified
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
                modifier = Modifier. size(visualWidth),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .padding(4.dp)
                ) {
                    WindCompassMini(
                        degrees = degrees,
                        windKph = windKph,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier. weight(1f))

            // ✅ Icons με ZERO padding
            // ✅ Icons in ROW με ΣΩΣΤΟ spacing όπως τις άλλες γραμμές
            Row(
                horizontalArrangement = Arrangement. spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (infoDescription != null) {
                    IconButton(
                        onClick = { showInfo = !showInfo },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 2.dp)  // ✅ Όπως πριν
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.information),
                            contentDescription = "Info $label",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)  // ✅ FULL size
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
                        .padding(start = 2.dp)  // ✅ Όπως πριν
                ) {
                    Icon(
                        painter = painterResource(R.drawable.copy),
                        contentDescription = "Copy $label",
                        tint = Color.Unspecified
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
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (infoDescription != null) {
            AnimatedVisibility(visible = showInfo) {
                Box(
                    modifier = Modifier
                        . fillMaxWidth()
                        . padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10. dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = infoDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme. surface
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
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun WeatherAlignedHumidityRow(
    index: Int,
    iconRes: Int,
    label: String,
    percentage: Float,
    textWidth: Dp,
    visualWidth: Dp,
    infoDescription: String?
) {
    val clipboardManager = LocalClipboardManager.current
    val clamped = percentage.coerceIn(0f, 100f)
    val bgColor = deviceInfoFieldBackground(index)
    var showInfo by rememberSaveable("${label}_${index}_humidity_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable("${label}_${index}_humidity_copy") { mutableStateOf(false) }

    val valueText = String.format(Locale.US, "%.3f%%", clamped)

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
                style = MaterialTheme. typography.labelMedium,
                modifier = Modifier.width(FieldIndexColumnWidth),
                textAlign = TextAlign.End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(29. dp)
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
                                color = MaterialTheme. colorScheme.primary
                            )
                        ) {
                            append(valueText)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow. Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.size(visualWidth),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        . padding(4.dp)
                ) {
                    PercentageDonut(
                        modifier = Modifier.fillMaxSize(),
                        percentage = clamped,
                        baseColor = MaterialTheme.colorScheme.surfaceVariant,
                        progressColor = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp,
                        label = valueText
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ✅ Icons με ZERO padding
            // ✅ Icons in ROW με ΣΩΣΤΟ spacing όπως τις άλλες γραμμές
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (infoDescription != null) {
                    IconButton(
                        onClick = { showInfo = !showInfo },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 2.dp)  // ✅ Όπως πριν
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.information),
                            contentDescription = "Info $label",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)  // ✅ FULL size
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
                        .padding(start = 2.dp)  // ✅ Όπως πριν
                ) {
                    Icon(
                        painter = painterResource(R.drawable.copy),
                        contentDescription = "Copy $label",
                        tint = Color.Unspecified
                    )
                }
            }
        }

        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    . fillMaxWidth()
                    . padding(start = 8.dp, end = 12.dp, bottom = 2.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography. labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (infoDescription != null) {
            AnimatedVisibility(visible = showInfo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12. dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme. colorScheme.onSurface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
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
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}

// ✅ WindCompassMini με ΑΚΡΙΒΩΣ ίδιο stroke και μέγεθος κύκλου
@Composable
private fun WindCompassMini(
    degrees: Float?,
    windKph: Double,
    modifier: Modifier = Modifier
) {
    val outline = MaterialTheme.colorScheme.outlineVariant. copy(alpha = 0.8f)
    val accent = MaterialTheme.colorScheme.primary
    val faint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    val strokeWidth = 6.dp  // ✅ EXACT same as donut

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = strokeWidth.toPx()

        // ✅ EXACT same calculation as donut
        val r = (minOf(w, h) - strokePx) / 2f
        val center = Offset(w / 2f, h / 2f)

        // Outer circle - EXACT same as donut
        drawCircle(
            color = outline,
            radius = r,
            center = center,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // Cross lines
        val inset = r * 0.08f

        drawLine(
            color = faint,
            start = Offset(center.x, center.y - r + inset),
            end = Offset(center.x, center. y + r - inset),
            strokeWidth = w * 0.02f
        )
        drawLine(
            color = faint,
            start = Offset(center.x - r + inset, center.y),
            end = Offset(center. x + r - inset, center.y),
            strokeWidth = w * 0.02f
        )

        // Cardinal letters
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = (r * 0.28f).coerceAtLeast(12f)
            color = labelColor. toArgb()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        fun drawCenteredLetter(letter: String, x: Float, y: Float) {
            val tw = paint.measureText(letter)
            val fm = paint.fontMetrics
            val baseline = y - (fm.ascent + fm.descent) / 2f
            drawContext.canvas.nativeCanvas.drawText(
                letter,
                x - tw / 2f,
                baseline,
                paint
            )
        }

        val letterInset = r * 0.18f
        drawCenteredLetter("N", center.x, center.y - r + letterInset)
        drawCenteredLetter("S", center.x, center.y + r - letterInset)
        drawCenteredLetter("E", center.x + r - letterInset, center. y)
        drawCenteredLetter("W", center.x - r + letterInset, center.y)

        // Arrow
        if (degrees != null) {
            val windSpeedClamped = windKph.toFloat(). coerceIn(0f, 100f)
            val lengthFraction = 0.30f + (windSpeedClamped / 100f) * 0.55f
            val arrowLen = r * lengthFraction

            val angleRad = Math.toRadians(degrees. toDouble() - 90.0)
            val tip = Offset(
                x = center.x + (cos(angleRad) * arrowLen). toFloat(),
                y = center.y + (sin(angleRad) * arrowLen).toFloat()
            )

            drawLine(
                color = accent,
                start = center,
                end = tip,
                strokeWidth = w * 0.05f,
                cap = StrokeCap.Round
            )

            // Arrow head
            val headSize = r * 0.18f
            val leftAngle = angleRad + Math.toRadians(150.0)
            val rightAngle = angleRad - Math.toRadians(150.0)

            val left = Offset(
                x = tip.x + (cos(leftAngle) * headSize).toFloat(),
                y = tip.y + (sin(leftAngle) * headSize). toFloat()
            )
            val right = Offset(
                x = tip.x + (cos(rightAngle) * headSize).toFloat(),
                y = tip.y + (sin(rightAngle) * headSize).toFloat()
            )

            drawLine(
                color = accent,
                start = tip,
                end = left,
                strokeWidth = w * 0.04f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = accent,
                start = tip,
                end = right,
                strokeWidth = w * 0.04f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun CpuInfoSpecialThanksRow() {
    val linkColor = Color(0xFF1565C0) // blue link style

    val annotatedText = buildAnnotatedString {
        append("Special thanks: ")

        withLink(
            LinkAnnotation.Url(
                url = "https://github.com/pytorch/cpuinfo",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append("pytorch")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.heart),
            contentDescription = "Love",
            modifier = Modifier
                .size(18.dp)
                .padding(end = 6.dp),
            tint = Color.Unspecified
        )

        Text(
            text = annotatedText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun MobeetestSectionFields(
    info: MobeetestInfo,
    iconRes: Int
) {
    // buildMobeetestRows ΔΕΝ πρέπει να περιέχει RAM/CPU usage
    val rows = remember(info) { buildMobeetestRows(info) }

    val hasRam = info.ramPercentageUsage != null
    val hasCpu = info.cpuPercentageUsage != null

    val totalCount = rows.size +
            (if (hasRam) 1 else 0) +
            (if (hasCpu) 1 else 0)

    var currentIndex = 1

    // 1) Text rows
    // 1) Base rows (text/date/datetime)
    rows.forEach { row ->
        when (row.type) {
            MobeetestRowType.TEXT -> {
                DeviceInfoValueRow(
                    index = currentIndex,
                    iconRes = iconRes,
                    label = row.label,
                    value = row.value,
                    infoDescription = row.infoDescription,
                    showBottomDivider = currentIndex < totalCount
                )
            }

            MobeetestRowType.DATE -> {
                DeviceInfoDateFieldRow(
                    index = currentIndex,
                    iconRes = iconRes,
                    label = row.label,
                    millis = row.millis ?: 0L,
                    valueOverride = row.value,
                    infoDescription = row.infoDescription,
                    showBottomDivider = currentIndex < totalCount
                )
            }

            MobeetestRowType.DATETIME -> {
                DeviceInfoDateTimeFieldRow(
                    index = currentIndex,
                    iconRes = iconRes,
                    label = row.label,
                    millis = row.millis ?: 0L,
                    valueOverride = row.value,
                    infoDescription = row.infoDescription,
                    showBottomDivider = currentIndex < totalCount
                )
            }
        }
        currentIndex++
    }


    // 2) RAM donut
    info.ramPercentageUsage?.let { ram ->
        DeviceInfoPercentageFieldRow(
            index = currentIndex,
            iconRes = iconRes,
            label = "RAM usage",
            percentage = ram,
            unit = "%",
            infoDescription = "Approximate system RAM usage at the time of measurement.",
            showBottomDivider = currentIndex < totalCount
        )
        currentIndex++
    }

    // 3) CPU donut
    info.cpuPercentageUsage?.let { cpu ->
        DeviceInfoPercentageFieldRow(
            index = currentIndex,
            iconRes = iconRes,
            label = "CPU usage",
            percentage = cpu,
            unit = "%",
            infoDescription = "Approximate CPU usage at the time of measurement.",
            showBottomDivider = false
        )
    }
}

// -------------------- WEATHER MINI VISUALS --------------------

private fun temperatureColor(tempC: Double?): @Composable () -> Color = {
    when (temperatureBucket(tempC)) {
        TemperatureBucket.FREEZING -> MaterialTheme.colorScheme.secondary
        TemperatureBucket.COLD -> MaterialTheme.colorScheme.primary
        TemperatureBucket.MILD -> MaterialTheme.colorScheme.tertiary
        TemperatureBucket.HOT -> MaterialTheme.colorScheme.error
    }
}

private fun windDirToDegrees(dir: String?): Float? {
    val d = dir?.trim()?.uppercase(Locale.US) ?: return null
    return when (d) {
        "N" -> 0f
        "NNE" -> 22.5f
        "NE" -> 45f
        "ENE" -> 67.5f
        "E" -> 90f
        "ESE" -> 112.5f
        "SE" -> 135f
        "SSE" -> 157.5f
        "S" -> 180f
        "SSW" -> 202.5f
        "SW" -> 225f
        "WSW" -> 247.5f
        "W" -> 270f
        "WNW" -> 292.5f
        "NW" -> 315f
        "NNW" -> 337.5f
        else -> null
    }
}



// ✅ Update DeviceInfoWindCompassFieldRow to pass windKph:

/*
@Composable
fun DeviceInfoDateFieldRow(
    index: Int,
    iconRes: Int,
    label: String,
    millis: Long,
    modifier: Modifier = Modifier,
    valueOverride: String? = null,
    infoDescription: String? = null,
    showBottomDivider: Boolean = true
) {
    val clipboardManager = LocalClipboardManager.current
    var showInfo by rememberSaveable(label + index.toString() + "_date_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(label + index.toString() + "_date_copy") { mutableStateOf(false) }

    val bgColor = deviceInfoFieldBackground(index)
    val displayValue = valueOverride ?: formatDate(millis)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        // Header row (match style with other fields)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(FieldIndexColumnWidth),
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

            val color = MaterialTheme.colorScheme.primary
            val titleText = remember(label, displayValue) {
                buildAnnotatedString {
                    append(label)
                    append(": ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    ) {
                        append(displayValue)
                    }
                }
            }

            Text(
                text = titleText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier.size(24.dp).padding(end = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    val copyText = "$index. $label: $displayValue"
                    clipboardManager.setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.copy),
                    contentDescription = "Copy $label",
                    tint = Color.Unspecified
                )
            }
        }

        // Copied hint
        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .padding(start = 48.dp, end = 12.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Copied!",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Info tooltip
        AnimatedVisibility(visible = showInfo && infoDescription != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 12.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = infoDescription.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }

        LaunchedEffect(showInfo) {
            if (showInfo) {
                delay(4_000)
                showInfo = false
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
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

 */


@Composable
private fun MiniMonthCalendar(
    millis: Long,
    modifier: Modifier = Modifier
) {
    if (millis <= 0L) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(10.dp)
        ) {
            Text("Unknown date", style = MaterialTheme.typography.labelSmall)
        }
        return
    }

    val baseCal = remember(millis) {
        Calendar.getInstance().apply { timeInMillis = millis }
    }
    val monthCal = remember(millis) {
        (baseCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val selectedDay = baseCal.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val firstDow = monthCal.get(Calendar.DAY_OF_WEEK)
    val weekStart = monthCal.firstDayOfWeek

    fun dowIndex(dow: Int, start: Int): Int {
        var idx = dow - start
        if (idx < 0) idx += 7
        return idx
    }

    val offset = dowIndex(firstDow, weekStart)

    val cells = remember(millis) {
        MutableList<Int?>(42) { null }.also { list ->
            var day = 1
            var i = offset
            while (day <= daysInMonth && i < list.size) {
                list[i] = day
                day++
                i++
            }
        }
    }

    val monthTitle = remember(millis) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(monthCal.timeInMillis))
    }

    val dayNames = remember(weekStart) {
        val tmp = Calendar.getInstance()
        val names = mutableListOf<String>()
        for (i in 0..6) {
            val dow = ((weekStart - 1 + i) % 7) + 1
            tmp.set(Calendar.DAY_OF_WEEK, dow)
            names += SimpleDateFormat("EE", Locale.getDefault()).format(tmp.time)
        }
        names
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .padding(10.dp)
    ) {
        Text(
            text = monthTitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth()) {
            dayNames.forEach { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        for (row in 0 until 6) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val day = cells[row * 7 + col]
                    val isSelected = day != null && day == selectedDay

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                else
                                    Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                else
                                    Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day?.toString() ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun ThermometerMini(
    tempC: Double,
    modifier: Modifier = Modifier,
    minC: Float = -10f,
    maxC: Float = 40f
) {
    val clamped = tempC. toFloat(). coerceIn(minC, maxC)
    val fraction = ((clamped - minC) / (maxC - minC)). coerceIn(0f, 1f)

    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
    val mercury = temperatureColor(tempC). invoke()
    val tick = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val currentTempColor = Color(0xFF1565C0) // Blue
    val scaleLabelColor = Color.Black

    val labelText = String.format(Locale.US, "%.1f°C", tempC)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Geometry
        val bulbRadius = w * 0.28f
        val tubeWidth = w * 0.28f
        val tubeLeft = (w - tubeWidth) / 2f
        val tubeRight = tubeLeft + tubeWidth
        val tubeTop = h * 0.08f
        val tubeBottom = h - bulbRadius * 2.1f
        val tubeHeight = (tubeBottom - tubeTop).coerceAtLeast(1f)

        // Outline tube
        drawRoundRect(
            color = outline,
            topLeft = Offset(tubeLeft, tubeTop),
            size = Size(tubeWidth, tubeHeight),
            cornerRadius = CornerRadius(tubeWidth / 2f, tubeWidth / 2f),
            style = Stroke(width = w * 0.06f)
        )

        // Bulb outline
        val bulbCenter = Offset(w / 2f, h - bulbRadius)
        drawCircle(
            color = outline,
            radius = bulbRadius,
            center = bulbCenter,
            style = Stroke(width = w * 0.06f)
        )

        // Ticks (5)
        val tickCount = 5
        for (i in 0 until tickCount) {
            val y = tubeTop + (tubeHeight * i / (tickCount - 1).toFloat())
            drawLine(
                color = tick,
                start = Offset(tubeRight + w * 0.06f, y),
                end = Offset(tubeRight + w * 0.22f, y),
                strokeWidth = w * 0.03f)
        }

        // Mercury column
        val mercuryInset = w * 0.06f
        val innerLeft = tubeLeft + mercuryInset
        val innerWidth = tubeWidth - 2 * mercuryInset
        val mercuryHeight = tubeHeight * fraction
        val mercuryTop = tubeTop + tubeHeight - mercuryHeight

        drawRoundRect(
            color = mercury,
            topLeft = Offset(innerLeft, mercuryTop),
            size = Size(innerWidth, mercuryHeight),
            cornerRadius = CornerRadius(innerWidth / 2f, innerWidth / 2f)
        )

        // Mercury bulb fill
        drawCircle(
            color = mercury,
            radius = bulbRadius * 0.78f,
            center = bulbCenter
        )

        // ✅ Current temperature label (LEFT side, blue, bold, LARGER)
        val currentPaint = Paint().apply {
            isAntiAlias = true
            textSize = (w * 0.40f).coerceAtLeast(14f) // ✅ INCREASED from 0.32f
            color = currentTempColor.toArgb()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align. RIGHT // ✅ Right-align for left side
        }

        val currentTextX = tubeLeft - w * 0.08f // ✅ LEFT side (before tube)
        val currentFm = currentPaint.fontMetrics
        val currentBaselineY = (mercuryTop - (currentFm.ascent + currentFm.descent) / 2f)
            .coerceIn(currentPaint.textSize, h - currentPaint.textSize)

        drawContext.canvas. nativeCanvas.drawText(
            labelText,
            currentTextX,
            currentBaselineY,
            currentPaint
        )

        // ✅ Scale labels (RIGHT side, black, normal) - AFTER current temp
        val scaleTemps = listOf(-10f, 0f, 20f, 40f)
        val scalePaint = Paint().apply {
            isAntiAlias = true
            textSize = (w * 0.24f).coerceAtLeast(9f)
            color = scaleLabelColor.toArgb()
            typeface = Typeface.DEFAULT
        }

        scaleTemps.forEach { scaleTemp ->
            // Skip if too close to current temperature
            if (kotlin.math.abs(scaleTemp - clamped) < 3f) return@forEach

            if (scaleTemp in minC..maxC) {
                val scaleFraction = ((scaleTemp - minC) / (maxC - minC)).coerceIn(0f, 1f)
                val scaleY = tubeTop + tubeHeight - (tubeHeight * scaleFraction)

                val scaleText = "${scaleTemp.toInt()}°C"
                val scaleX = tubeRight + w * 0.35f // RIGHT side
                val fm = scalePaint.fontMetrics
                val baselineY = (scaleY - (fm.ascent + fm.descent) / 2f)
                    .coerceIn(scalePaint.textSize, h - scalePaint.textSize)

                drawContext.canvas. nativeCanvas.drawText(
                    scaleText,
                    scaleX,
                    baselineY,
                    scalePaint
                )
            }
        }
    }
}


/*
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
    val clipboardManager = LocalClipboardManager.current
    var showInfo by rememberSaveable(label + index. toString() + "_datetime_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable(label + index.toString() + "_datetime_copy") { mutableStateOf(false) }

    val bgColor = deviceInfoFieldBackground(index)
    val displayValue = valueOverride ?: formatDateTime(millis)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
    ) {
        // Header row (index, icon, label, info, copy)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12. dp, top = 6.dp, bottom = 6. dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(FieldIndexColumnWidth),
                textAlign = TextAlign. End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(29.dp)
                    .padding(start = 8.dp, end = 4.dp),
                tint = Color.Unspecified
            )

            val color = MaterialTheme.colorScheme.primary
            val titleText = remember(label, displayValue) {
                buildAnnotatedString {
                    append(label)
                    append(": ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    ) {
                        append(displayValue)
                    }
                }
            }

            Text(
                text = titleText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier. size(24.dp).padding(end = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    val copyText = "$index.  $label: $displayValue"
                    clipboardManager.setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier.size(24.dp). padding(end = 2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.copy),
                    contentDescription = "Copy $label",
                    tint = Color. Unspecified
                )
            }
        }

        // Copied message
        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12. dp, bottom = 2.dp),
                contentAlignment = Alignment. CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Info tooltip
        if (infoDescription != null) {
            AnimatedVisibility(visible = showInfo) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12. dp, end = 12.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.onSurface. copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8. dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6. dp)
                    ) {
                        Text(
                            text = infoDescription,
                            style = MaterialTheme.typography. bodySmall,
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

        // Calendar + Clock body
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically  // ✅ CHANGED from Alignment.Top
        ) {
            MiniMonthCalendar(
                millis = millis,
                modifier = Modifier. weight(1f)
            )
            MiniAnalogClock(
                millis = millis,
                modifier = Modifier. weight(0.6f)
            )
        }

        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme. colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

 */

@Composable
private fun MiniAnalogClock(
    millis: Long,
    modifier: Modifier = Modifier
) {
    if (millis <= 0L) {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant. copy(alpha = 0.4f))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Unknown time", style = MaterialTheme.typography.labelSmall)
        }
        return
    }

    val cal = remember(millis) {
        Calendar.getInstance().apply { timeInMillis = millis }
    }
    val hour = cal.get(Calendar.HOUR)
    val minute = cal.get(Calendar.MINUTE)
    val second = cal.get(Calendar.SECOND)

    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val background = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val handColor = MaterialTheme.colorScheme.primary
    val secondHandColor = MaterialTheme.colorScheme.error. copy(alpha = 0.7f)
    val tickColor = MaterialTheme.colorScheme. onSurface.copy(alpha = 0.25f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, outline, RoundedCornerShape(12.dp))
            .background(background)
            .padding(10.dp)
    ) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Draw clock circle
        drawCircle(
            color = outline,
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw hour markers and numbers
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = radius * 0.25f
            color = labelColor. toArgb()
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align. CENTER
        }

        for (i in 1..12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val isMainHour = i % 3 == 0

            if (isMainHour) {
                // Draw number
                val textRadius = radius * 0.75f
                val x = center.x + (cos(angle) * textRadius).toFloat()
                val y = center.y + (sin(angle) * textRadius).toFloat()
                val fm = paint.fontMetrics
                val baselineY = y - (fm.ascent + fm. descent) / 2f

                drawContext.canvas.nativeCanvas.drawText(
                    i.toString(),
                    x,
                    baselineY,
                    paint
                )
            } else {
                // Draw small tick
                val tickStart = radius * 0.85f
                val tickEnd = radius * 0.92f
                val startX = center.x + (cos(angle) * tickStart).toFloat()
                val startY = center.y + (sin(angle) * tickStart).toFloat()
                val endX = center.x + (cos(angle) * tickEnd).toFloat()
                val endY = center.y + (sin(angle) * tickEnd). toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Draw hour hand
        val hourAngle = Math.toRadians(((hour % 12) * 30 + minute * 0.5 - 90))
        val hourLength = radius * 0.5f
        val hourEnd = Offset(
            x = center.x + (cos(hourAngle) * hourLength). toFloat(),
            y = center.y + (sin(hourAngle) * hourLength).toFloat()
        )
        drawLine(
            color = handColor,
            start = center,
            end = hourEnd,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw minute hand
        val minuteAngle = Math. toRadians((minute * 6 - 90).toDouble())
        val minuteLength = radius * 0.7f
        val minuteEnd = Offset(
            x = center.x + (cos(minuteAngle) * minuteLength).toFloat(),
            y = center.y + (sin(minuteAngle) * minuteLength).toFloat()
        )
        drawLine(
            color = handColor,
            start = center,
            end = minuteEnd,
            strokeWidth = 2. dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw second hand (thin, red/error color)
        val secondAngle = Math.toRadians((second * 6 - 90).toDouble())
        val secondLength = radius * 0.8f
        val secondEnd = Offset(
            x = center. x + (cos(secondAngle) * secondLength).toFloat(),
            y = center.y + (sin(secondAngle) * secondLength).toFloat()
        )
        drawLine(
            color = secondHandColor,
            start = center,
            end = secondEnd,
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw center dot
        drawCircle(
            color = handColor,
            radius = 4. dp.toPx(),
            center = center
        )
    }
}


private fun formatDateTimeNoSeconds(millis: Long): String {
    if (millis <= 0L) return "Unknown"
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale. getDefault()).format(Date(millis))
}



@Composable
private fun DateHeaderRow(
    index: Int,
    iconRes: Int,
    label: String,
    displayValue: String,
    infoDescription: String?,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var showInfo by rememberSaveable("${label}_${index}_date_header_info") { mutableStateOf(false) }
    var showCopied by rememberSaveable("${label}_${index}_date_header_copy") { mutableStateOf(false) }

    Column(modifier = modifier. fillMaxWidth()) {
        // Header row (index, icon, label, info, copy)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.width(FieldIndexColumnWidth),
                textAlign = TextAlign.End
            )

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(29.dp)
                    .padding(start = 8.dp, end = 4.dp),
                tint = Color. Unspecified
            )

            val color = MaterialTheme.colorScheme.primary
            val titleText = remember(label, displayValue) {
                buildAnnotatedString {
                    append(label)
                    append(": ")
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    ) {
                        append(displayValue)
                    }
                }
            }

            Text(
                text = titleText,
                style = MaterialTheme. typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (infoDescription != null) {
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier.size(24.dp). padding(end = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.information),
                        contentDescription = "Info $label",
                        tint = Color.Unspecified
                    )
                }
            }

            IconButton(
                onClick = {
                    val copyText = "$index.  $label: $displayValue"
                    clipboardManager.setText(AnnotatedString(copyText))
                    showCopied = true
                },
                modifier = Modifier.size(24.dp).padding(end = 2. dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.copy),
                    contentDescription = "Copy $label",
                    tint = Color.Unspecified
                )
            }
        }

        // Copied message
        AnimatedVisibility(visible = showCopied) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 12.dp, bottom = 2.dp),
                contentAlignment = Alignment. CenterEnd
            ) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Info tooltip
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
                                shape = RoundedCornerShape(8.dp)
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
    }
}

// Now refactor DeviceInfoDateFieldRow (around line 2650):

@Composable
fun DeviceInfoDateFieldRow(
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
    val displayValue = valueOverride ?: formatDate(millis)

    Column(
        modifier = modifier
            .fillMaxWidth()
            . background(bgColor)
    ) {
        // Use common header
        DateHeaderRow(
            index = index,
            iconRes = iconRes,
            label = label,
            displayValue = displayValue,
            infoDescription = infoDescription
        )

        // Calendar body
        MiniMonthCalendar(
            millis = millis,
            modifier = Modifier
                . padding(start = 48.dp, end = 12.dp, bottom = 8.dp)
        )

        if (showBottomDivider) {
            HorizontalDivider(
                modifier = Modifier. fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant. copy(alpha = 0.4f)
            )
        }
    }
}

// Refactor DeviceInfoDateTimeFieldRow (around line 2700):

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
            infoDescription = infoDescription
        )

        // Calendar + Clock body
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement. spacedBy(10.dp),
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
                modifier = Modifier. fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant. copy(alpha = 0.4f)
            )
        }
    }
}