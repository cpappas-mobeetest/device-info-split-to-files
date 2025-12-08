package com.mobeetest.worker.sharedPreferences.permissions

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPrefsManager {

    private const val PREFS_NAME = "mobeetest_prefs"

    // Keys
    private const val KEY_PLAY_SOUND = "play_sound"

    // Requested flags
    private const val KEY_REQ_BATTERY = "requested_battery"
    private const val KEY_REQ_LOCATION = "requested_location"
    private const val KEY_REQ_BACKGROUND = "requested_background"
    private const val KEY_REQ_NOTIFICATIONS = "requested_notifications"
    private const val KEY_REQ_PHONE = "requested_phone"
    private const val KEY_REQ_CAMERA = "requested_camera"
    private const val KEY_REQ_PAUSE = "requested_pause"
    private const val KEY_REQ_WIFI = "requested_wifi"

    // Last known permission status
    private const val KEY_LAST_BATTERY = "last_battery_granted"
    private const val KEY_LAST_LOCATION = "last_location_granted"
    private const val KEY_LAST_BACKGROUND = "last_background_granted"
    private const val KEY_LAST_NOTIFICATIONS = "last_notifications_granted"
    private const val KEY_LAST_PHONE = "last_phone_granted"
    private const val KEY_LAST_CAMERA = "last_camera_granted"
    private const val KEY_LAST_PAUSE = "last_pause_granted"
    private const val KEY_LAST_WIFI = "last_wifi_granted"

    // Permission wizard state (used by PermissionsActivity)
    private const val KEY_PERMISSION_STEPS = "permission_steps"
    private const val KEY_PERMS_CHECKED_AFTER_INSTALL = "permissions_checked_after_installation"
    private const val KEY_CAN_SERVICE_START = "can_service_start"

    // MainActivity / first-run flag for PermissionsActivity
    private const val KEY_PERMISSIONS_ACTIVITY_SHOWN = "permissions_activity_shown"

    // --------------------------------------------------
    // Permission wizard serialized steps
    // --------------------------------------------------
    fun setPermissionSteps(context: Context, serialized: String) {
        prefs(context).edit { putString(KEY_PERMISSION_STEPS, serialized) }
    }

    fun getPermissionSteps(context: Context): String {
        return prefs(context).getString(KEY_PERMISSION_STEPS, "") ?: ""
    }

    fun clearPermissionSteps(context: Context) {
        prefs(context).edit { remove(KEY_PERMISSION_STEPS) }
    }

    // --------------------------------------------------
    // Permissions checked after installation flag
    // --------------------------------------------------
    fun setPermissionsCheckedAfterInstallation(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_PERMS_CHECKED_AFTER_INSTALL, value) }
    }

    @Suppress("unused")
    fun getPermissionsCheckedAfterInstallation(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_PERMS_CHECKED_AFTER_INSTALL, false)
    }

    // --------------------------------------------------
    // Service start flag
    // --------------------------------------------------
    fun setCanServiceStart(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_CAN_SERVICE_START, value) }
    }

    @Suppress("unused")
    fun getCanServiceStart(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_CAN_SERVICE_START, false)
    }

    // --------------------------------------------------
    // PermissionsActivity shown flag (for MainActivity)
    // --------------------------------------------------
    @Suppress("unused")
    fun setPermissionsActivityShown(context: Context, shown: Boolean) {
        prefs(context).edit { putBoolean(KEY_PERMISSIONS_ACTIVITY_SHOWN, shown) }
    }

    @Suppress("unused")
    fun hasPermissionsActivityBeenShown(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_PERMISSIONS_ACTIVITY_SHOWN, false)
    }

    // --------------------------------------------------
    // Generic SharedPrefs accessor
    // --------------------------------------------------
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --------------------------------------------------
    // Play Sounds Setting
    // --------------------------------------------------
    fun setPlaySound(context: Context, enabled: Boolean) {
        prefs(context).edit { putBoolean(KEY_PLAY_SOUND, enabled) }
    }

    fun getPlaySound(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PLAY_SOUND, true)

    // --------------------------------------------------
    // Helper for requested flags
    // --------------------------------------------------
    private fun setRequested(context: Context, key: String, value: Boolean) {
        prefs(context).edit { putBoolean(key, value) }
    }

    private fun getRequested(context: Context, key: String): Boolean =
        prefs(context).getBoolean(key, false)

    // --------------------------------------------------
    // Helper for last-known permissions
    // --------------------------------------------------
    private fun setLastGranted(context: Context, key: String, granted: Boolean) {
        prefs(context).edit { putBoolean(key, granted) }
    }

    private fun getLastGranted(context: Context, key: String): Boolean =
        prefs(context).getBoolean(key, false)

    // --------------------------------------------------
    // PUBLIC API
    // --------------------------------------------------

    // Battery
    @Suppress("unused")
    fun setRequestedBattery(context: Context, value: Boolean) =
        setRequested(context, KEY_REQ_BATTERY, value)

    @Suppress("unused")
    fun getRequestedBattery(context: Context): Boolean =
        getRequested(context, KEY_REQ_BATTERY)

    fun setLastBatteryGranted(context: Context, granted: Boolean) =
        setLastGranted(context, KEY_LAST_BATTERY, granted)

    @Suppress("unused")
    fun getLastBatteryGranted(context: Context): Boolean =
        getLastGranted(context, KEY_LAST_BATTERY)

    // Location
    fun setRequestedLocation(context: Context, value: Boolean) =
        setRequested(context, KEY_REQ_LOCATION, value)

    fun getRequestedLocation(context: Context): Boolean =
        getRequested(context, KEY_REQ_LOCATION)

    fun setLastLocationGranted(context: Context, granted: Boolean) =
        setLastGranted(context, KEY_LAST_LOCATION, granted)

    @Suppress("unused")
    fun getLastLocationGranted(context: Context): Boolean =
        getLastGranted(context, KEY_LAST_LOCATION)

    // Background Location
    fun setRequestedBackground(context: Context, value: Boolean) =
        setRequested(context, KEY_REQ_BACKGROUND, value)

    fun getRequestedBackground(context: Context): Boolean =
        getRequested(context, KEY_REQ_BACKGROUND)

    fun setLastBackgroundGranted(context: Context, granted: Boolean) =
        setLastGranted(context, KEY_LAST_BACKGROUND, granted)

    @Suppress("unused")
    fun getLastBackgroundGranted(context: Context): Boolean =
        getLastGranted(context, KEY_LAST_BACKGROUND)

    // Notifications
    fun setRequestedNotifications(context: Context, value: Boolean) =
        setRequested(context, KEY_REQ_NOTIFICATIONS, value)

    fun getRequestedNotifications(context: Context): Boolean =
        getRequested(context, KEY_REQ_NOTIFICATIONS)

    fun setLastNotificationsGranted(context: Context, granted: Boolean) =
        setLastGranted(context, KEY_LAST_NOTIFICATIONS, granted)

    @Suppress("unused")
    fun getLastNotificationsGranted(context: Context): Boolean =
        getLastGranted(context, KEY_LAST_NOTIFICATIONS)

    // Phone State
    fun setRequestedPhone(context: Context, value: Boolean) =
        setRequested(context, KEY_REQ_PHONE, value)

    fun getRequestedPhone(context: Context): Boolean =
        getRequested(context, KEY_REQ_PHONE)

    fun setLastPhoneGranted(context: Context, granted: Boolean) =
        setLastGranted(context, KEY_LAST_PHONE, granted)

    @Suppress("unused")
    fun getLastPhoneGranted(context: Context): Boolean =
        getLastGranted(context, KEY_LAST_PHONE)

    // Camera
    fun setRequestedCamera(context: Context, value: Boolean) =
        setRequested(context, KEY_REQ_CAMERA, value)

    fun getRequestedCamera(context: Context): Boolean =
        getRequested(context, KEY_REQ_CAMERA)

    fun setLastCameraGranted(context: Context, granted: Boolean) =
        setLastGranted(context, KEY_LAST_CAMERA, granted)

    @Suppress("unused")
    fun getLastCameraGranted(context: Context): Boolean =
        getLastGranted(context, KEY_LAST_CAMERA)

    // Pause Unused
    @Suppress("unused")
    fun setRequestedPause(context: Context, value: Boolean) =
        setRequested(context, KEY_REQ_PAUSE, value)

    @Suppress("unused")
    fun getRequestedPause(context: Context): Boolean =
        getRequested(context, KEY_REQ_PAUSE)

    fun setLastPauseGranted(context: Context, granted: Boolean) =
        setLastGranted(context, KEY_LAST_PAUSE, granted)

    @Suppress("unused")
    fun getLastPauseGranted(context: Context): Boolean =
        getLastGranted(context, KEY_LAST_PAUSE)

    // Nearby Wi-Fi
    fun setRequestedWifi(context: Context, value: Boolean) =
        setRequested(context, KEY_REQ_WIFI, value)

    fun getRequestedWifi(context: Context): Boolean =
        getRequested(context, KEY_REQ_WIFI)

    fun setLastWifiGranted(context: Context, granted: Boolean) =
        setLastGranted(context, KEY_LAST_WIFI, granted)

    @Suppress("unused")
    fun getLastWifiGranted(context: Context): Boolean =
        getLastGranted(context, KEY_LAST_WIFI)


    // --------------------------------------------------
    // Reset όλων των permission-related SharedPreferences
    // ώστε να συμπεριφέρεται σαν fresh install
    // --------------------------------------------------
    fun clearAllPermissionData(context: Context) {
        prefs(context).edit {
            // Wizard / flow state
            remove(KEY_PERMISSION_STEPS)
            remove(KEY_PERMS_CHECKED_AFTER_INSTALL)
            remove(KEY_CAN_SERVICE_START)
            remove(KEY_PERMISSIONS_ACTIVITY_SHOWN)

            // Requested flags
            remove(KEY_REQ_BATTERY)
            remove(KEY_REQ_LOCATION)
            remove(KEY_REQ_BACKGROUND)
            remove(KEY_REQ_NOTIFICATIONS)
            remove(KEY_REQ_PHONE)
            remove(KEY_REQ_CAMERA)
            remove(KEY_REQ_PAUSE)
            remove(KEY_REQ_WIFI)

            // Last-known grants
            remove(KEY_LAST_BATTERY)
            remove(KEY_LAST_LOCATION)
            remove(KEY_LAST_BACKGROUND)
            remove(KEY_LAST_NOTIFICATIONS)
            remove(KEY_LAST_PHONE)
            remove(KEY_LAST_CAMERA)
            remove(KEY_LAST_PAUSE)
            remove(KEY_LAST_WIFI)
        }
    }
}
