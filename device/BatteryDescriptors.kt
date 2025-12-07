package com.mobeetest.worker.activities.main.pages.composables.device

import com.mobeetest.worker.data.model.device.BatteryHealth
import com.mobeetest.worker.data.model.device.BatteryStatus
import com.mobeetest.worker.data.model.device.ChargerConnection

internal fun describeBatteryHealth(health: BatteryHealth?): String =
    when (health) {
        BatteryHealth.GOOD -> "Good"
        BatteryHealth.FAILED -> "Failure"
        BatteryHealth.DEAD -> "Dead"
        BatteryHealth.OVERVOLTAGE -> "Over voltage"
        BatteryHealth.OVERHEATED -> "Overheated"
        BatteryHealth.UNKNOWN, null -> "Unknown"
    }

internal fun describeChargerConnection(conn: ChargerConnection?): String =
    when (conn) {
        ChargerConnection.AC -> "AC charger"
        ChargerConnection.USB -> "USB"
        ChargerConnection.WIRELESS -> "Wireless"
        ChargerConnection.NONE -> "On battery"
        ChargerConnection.UNKNOWN, null -> "Unknown"
    }

internal fun describeBatteryStatus(status: BatteryStatus?): String =
    when (status) {
        BatteryStatus.CHARGING -> "Charging"
        BatteryStatus.DISCHARGING -> "Discharging"
        BatteryStatus.NOT_CHARGING -> "Not charging"
        BatteryStatus.FULL -> "Full"
        BatteryStatus.UNKNOWN, null -> "Unknown"
    }
