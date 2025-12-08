package com.mobeetest.worker.services.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import com.mobeetest.worker.services.cellular.CellularService
import com.mobeetest.worker.services.location.LocationService
import com.mobeetest.worker.services.network.NetworkService
import com.mobeetest.worker.services.network_drive.NetworkDriveService
import com.mobeetest.worker.services.sensors.SensorsService
import com.mobeetest.worker.services.speed_test.SpeedTestService
import com.mobeetest.worker.services.wiFi.WiFiService
import com.mobeetest.worker.services.wiFi_drive.WiFiDriveService


//import kotlin.collections.contains

@ExperimentalFoundationApi
class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationService.ACTION_RUN_ALL -> {
                // Start all services
                Log.d("NotificationActionReceiver", "Run all triggered")
                startAllServices(context)
            }
            NotificationService.ACTION_PAUSE_ALL -> {
                Log.d("NotificationActionReceiver", "Pause all triggered")
                pauseAllServices(context)
            }
            NotificationService.ACTION_STOP_ALL -> {
                Log.d("NotificationActionReceiver", "Stop all triggered")
                stopAllServices(context)
            }
        }
    }

    private fun startAllServices(context: Context) {
        listOf(
            LocationService::class,
            SensorsService::class,
            NetworkService::class,
            NetworkDriveService::class,
            CellularService::class,
            SpeedTestService::class,
            WiFiService::class,
            WiFiDriveService::class
        ).forEach { serviceClass ->
            val intent = Intent(context, serviceClass.java).apply {
                action = "RUN_ALL"
            }
            context.startService(intent)
        }

        val broadcastIntent = Intent("com.mobeetest.worker.SERVICES_STATES").apply {
            putExtra("state", "RUN_ALL")
            putExtra("service_name", "ALL")
            setPackage(context.packageName) // ✅ σωστό
        }
        context.sendBroadcast(broadcastIntent) // ✅ σωστό

    }

    private fun pauseAllServices(context: Context) {
        listOf(
            LocationService::class,
            SensorsService::class,
            NetworkService::class,
            NetworkDriveService::class,
            CellularService::class,
            SpeedTestService::class,
            WiFiService::class,
            WiFiDriveService::class
        ).forEach { serviceClass ->
            val intent = Intent(context, serviceClass.java).apply {
                action = "PAUSE_ALL"
            }
            context.startService(intent)
        }

        val broadcastIntent = Intent("com.mobeetest.worker.SERVICES_STATES").apply {
            putExtra("state", "PAUSE_ALL")
            putExtra("service_name", "ALL")
            setPackage(context.packageName) // ✅ σωστό
        }
        context.sendBroadcast(broadcastIntent) // ✅ σωστό

    }

    private fun stopAllServices(context: Context) {
        listOf(
            LocationService::class,
            SensorsService::class,
            NetworkService::class,
            NetworkDriveService::class,
            CellularService::class,
            SpeedTestService::class,
            WiFiService::class,
            WiFiDriveService::class
        ).forEach { serviceClass ->
            val intent = Intent(context, serviceClass.java).apply {
                action = "STOP_ALL"
            }
            context.startService(intent)
        }

        val broadcastIntent = Intent("com.mobeetest.worker.SERVICES_STATES").apply {
            putExtra("state", "STOP_ALL")
            putExtra("service_name", "ALL")
            setPackage(context.packageName) // ✅ σωστό
        }
        context.sendBroadcast(broadcastIntent) // ✅ σωστό

    }
}
