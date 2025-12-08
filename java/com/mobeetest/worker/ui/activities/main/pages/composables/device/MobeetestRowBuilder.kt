package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.data.model.device.MobeetestInfo

internal fun buildMobeetestRows(info: MobeetestInfo?): List<MobeetestRowSpec> {
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
        infoDescription = "Which Mobeetest edition is currently unlocked on this device.",
        rowType = MobeetestRowType.VALUE
    )

    info.version.takeIf { it.isNotBlank() }?.let { version ->
        rows += MobeetestRowSpec(
            label = "Version",
            value = version,
            infoDescription = "Human-readable app version shown to users (e.g., 1.2.3). Provided from BuildConfig.",
            rowType = MobeetestRowType.VALUE
        )
    }

    info.installerPackage?.takeIf { it.isNotBlank() }?.let { installer ->
        rows += MobeetestRowSpec(
            label = "Installer package",
            value = installer,
            infoDescription = "Package that installed this APK (e.g. Google Play or sideload).",
            rowType = MobeetestRowType.VALUE
        )
    }

    rows += MobeetestRowSpec(
        label = "Installed on",
        value = formatDateTime(info.dateInstalled),
        infoDescription = "Timestamp when the app was first installed on the device.",
        rowType = MobeetestRowType.DATE_TIME,
        dateMillis = info.dateInstalled
    )

    rows += MobeetestRowSpec(
        label = "Last updated on",
        value = formatDateTime(info.dateLastUpdated),
        infoDescription = "Timestamp when the app was last updated on the device.",
        rowType = MobeetestRowType.DATE_TIME,
        dateMillis = info.dateLastUpdated
    )

    info.totalUpdates.let { updates ->
        rows += MobeetestRowSpec(
            label = "Total updates",
            value = updates.toString(),
            infoDescription = "How many different versions of the app have been installed since first install (counted locally).",
            rowType = MobeetestRowType.VALUE
        )
    }

    info.totalApplicationRunsSinceFirstInstall.let { runs ->
        rows += MobeetestRowSpec(
            label = "Total runs",
            value = runs.toString(),
            infoDescription = "How many times the app has been started since first install (tracked locally).",
            rowType = MobeetestRowType.VALUE
        )
    }

    val lastRun = info.lastRunAt ?: 0L

    rows += MobeetestRowSpec(
        label = "Last run at",
        value = formatDateTime(lastRun),
        infoDescription = "Timestamp of the most recent application run (based on local tracking).",
        rowType = MobeetestRowType.DATE_TIME,
        dateMillis = lastRun
    )


    info.totalActiveServices.let { active ->
        rows += MobeetestRowSpec(
            label = "Active services",
            value = active.toString(),
            infoDescription = "Number of Mobeetest background services that are currently running.",
            rowType = MobeetestRowType.VALUE
        )
    }


    info.fcmToken?.takeIf { it.isNotBlank() }?.let { token ->
        rows += MobeetestRowSpec(
            label = "FCM token",
            value = token,
            infoDescription = "Current Firebase Cloud Messaging registration token used for push notifications.",
            rowType = MobeetestRowType.VALUE
        )
    }

    info.fcmUuid?.takeIf { it.isNotBlank() }?.let { uuid ->
        rows += MobeetestRowSpec(
            label = "FCM UUID",
            value = uuid,
            infoDescription = "Stable UUID used to identify this installation for backend / push logic.",
            rowType = MobeetestRowType.VALUE
        )
    }

    info.ftpAvailableStorage?.takeIf { it > 0L }?.let { ftpBytes ->
        val mb = ftpBytes / (1024 * 1024)
        rows += MobeetestRowSpec(
            label = "FTP available storage",
            value = "$mb MB",
            infoDescription = "Free space reported by the configured FTP storage target (paid editions only).",
            rowType = MobeetestRowType.VALUE
        )
    }

    rows += MobeetestRowSpec(
        label = "Build Type",
        value = info.buildType,
        infoDescription = "Gradle build type used for this APK (debug / release). Useful for identifying test vs production builds.",
        rowType = MobeetestRowType.VALUE
    )

    rows += MobeetestRowSpec(
        label = "Application id",
        value = info.applicationId,
        infoDescription = "Unique application package name installed on the device. Used by Android and backend systems to identify this app.",
        rowType = MobeetestRowType.VALUE
    )

    return rows
}
