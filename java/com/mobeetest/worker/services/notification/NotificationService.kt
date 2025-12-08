package com.mobeetest.worker.services.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import com.mobeetest.worker.R
import kotlin.reflect.KClass

import com.mobeetest.worker.services.location.LocationService
import com.mobeetest.worker.services.sensors.SensorsService
import com.mobeetest.worker.services.network.NetworkService
import com.mobeetest.worker.services.network_drive.NetworkDriveService
import com.mobeetest.worker.services.cellular.CellularService
import com.mobeetest.worker.services.speed_test.SpeedTestService
import com.mobeetest.worker.services.wiFi.WiFiService
import com.mobeetest.worker.services.wiFi_drive.WiFiDriveService
import android.widget.RemoteViews
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.app.NotificationCompat


@ExperimentalFoundationApi
class NotificationService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        const val ACTION_RUN_ALL = "com.mobeetest.worker.ACTION_RUN_ALL"
        const val ACTION_PAUSE_ALL = "com.mobeetest.worker.ACTION_PAUSE_ALL"
        const val ACTION_STOP_ALL = "com.mobeetest.worker.ACTION_STOP_ALL"
    }

    private val channelId = "mobeetest_notification_id"

    override fun onCreate() {
        registerReceiver(
            serviceStateReceiver,
            IntentFilter("com.mobeetest.worker.SERVICES_STATES"),
            RECEIVER_NOT_EXPORTED
        )
        Log.d("NetworkService","registerReceiver serviceStateReceiver")
        super.onCreate()

        createNotificationChannel()

        startForeground(NOTIFICATION_ID, buildCustomExpandedNotification())

        // Mark all services as "Stop" at start
        // ❗ Delay default STOP map initialization


        Handler(mainLooper).post {
            if (stateToServiceClassListMap.isEmpty()) {
                val stopList = serviceClassToNameMap.keys.toMutableList()
                stateToServiceClassListMap["Stop"] = stopList
                refreshNotification()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //stopSelf() // Auto-stop after showing
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Mobeetest",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildCustomExpandedNotification(): Notification {
        val collapsedView = RemoteViews(packageName, R.layout.notification_collapsed).apply {
            setTextViewText(R.id.collapsed_title, "Mobeetest")
            setTextViewText(R.id.collapsed_summary, "Tap to view status")
        }

        val expandedView = RemoteViews(packageName, R.layout.notification_expanded)


        // Intent to open the main activity (Dashboard)
        // Intent to open/bring-forward MainActivity (Dashboard)
        val dashboardIntent = Intent(this, com.mobeetest.worker.MainActivity::class.java).apply {
            // If an instance exists in the current task, bring it forward.
            // If not, create a new one. If it’s already on screen, it’s a no-op.
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // Optional: if you trigger from a Service context, NEW_TASK is fine too:
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            dashboardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        expandedView.setOnClickPendingIntent(R.id.open_dashboard, pendingIntent)

        Log.d("NetworkService", "stateToServiceClassListMap: $stateToServiceClassListMap")

        // Always show Run, Pause, Stop first, in this order
        val orderedCoreStates = listOf("Run", "Pause", "Stop")

        for (coreState in orderedCoreStates) {
            val services = stateToServiceClassListMap[coreState] ?: emptyList()
            val serviceNames = services.mapNotNull { serviceClassToNameMap[it] }

            val stateBlock = RemoteViews(packageName, R.layout.notification_state_block)

            val statePrefix = "$coreState: "
            val serviceText = if (serviceNames.isEmpty()) "—" else serviceNames.joinToString(", ")
            val fullText = statePrefix + serviceText

            val spannable = SpannableString(fullText).apply {
                setSpan(
                    StyleSpan(android.graphics.Typeface.BOLD),
                    0,
                    statePrefix.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            stateBlock.setCharSequence(R.id.inline_state, "setText", spannable)
            expandedView.addView(R.id.expanded_root, stateBlock)
        }

        // Then render remaining states (excluding Run, Pause, Stop)
        stateToServiceClassListMap
            .filterKeys { it !in orderedCoreStates }
            .toSortedMap() // Optional: alphabetic order
            .forEach { (state, serviceClasses) ->
                if (serviceClasses.isEmpty()) return@forEach

                val serviceNames = serviceClasses.mapNotNull { serviceClassToNameMap[it] }
                val stateBlock = RemoteViews(packageName, R.layout.notification_state_block)

                val statePrefix = "$state: "
                val serviceText = serviceNames.joinToString(", ")
                val fullText = statePrefix + serviceText

                val spannable = SpannableString(fullText).apply {
                    setSpan(
                        StyleSpan(android.graphics.Typeface.BOLD),
                        0,
                        statePrefix.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                stateBlock.setCharSequence(R.id.inline_state, "setText", spannable)
                expandedView.addView(R.id.expanded_root, stateBlock)
            }

        val runAllIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = ACTION_RUN_ALL
        }
        val runAllPendingIntent = PendingIntent.getBroadcast(
            this, 1, runAllIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        expandedView.setOnClickPendingIntent(R.id.run_all, runAllPendingIntent)

        val pauseAllIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = ACTION_PAUSE_ALL
        }
        val pauseAllPendingIntent = PendingIntent.getBroadcast(
            this, 2, pauseAllIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        expandedView.setOnClickPendingIntent(R.id.pause_all, pauseAllPendingIntent)

        val stopAllIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = ACTION_STOP_ALL
        }
        val stopAllPendingIntent = PendingIntent.getBroadcast(
            this, 3, stopAllIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        expandedView.setOnClickPendingIntent(R.id.stop_all, stopAllPendingIntent)


        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle("Mobeetest")
            .setContentText("Services Running")
            .build()
    }

    val serviceClassToNameMap: Map<KClass<out Service>, String> = mapOf(
        LocationService::class to "Location",
        SensorsService::class to "Sensors",
        NetworkService::class to "Network",
        NetworkDriveService::class to "Drive Network",
        CellularService::class to "Cellular",
        SpeedTestService::class to "Speed Test",
        WiFiService::class to "WiFi",
        WiFiDriveService::class to "Drive WiFi",
    )

    val stateToServiceClassListMap: MutableMap<String, MutableList<KClass<out Service>>> = mutableMapOf()
    val serviceNameToClassMap: Map<String, KClass<out Service>> = serviceClassToNameMap.entries.associate { (k, v) -> v to k }


    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("NotificationService", "broadcast received")
            val stateRaw = intent?.getStringExtra("state") ?: return
            val state = stateRaw.lowercase().replaceFirstChar { it.uppercaseChar() }
            val serviceName = intent.getStringExtra("service_name") ?: return

            if (serviceName == "ALL"){
                // 🧹 Καθαρισμός όλων των καταστάσεων
                stateToServiceClassListMap.clear()

                val newState = when (stateRaw.uppercase()) {
                    "RUN_ALL" -> "Run"
                    "PAUSE_ALL" -> "Pause"
                    "STOP_ALL" -> "Stop"
                    else -> return
                }

                // ✅ Ενημέρωση με όλα τα services σε μία κατάσταση
                stateToServiceClassListMap[newState] = serviceClassToNameMap.keys.toMutableList()

                Log.d("NotificationService", "Applied ALL → $newState")

                refreshNotification()
            }else {

                val serviceClass = serviceNameToClassMap[serviceName] ?: return

                // 🛠️ Remove the service from all states before assigning new one
                stateToServiceClassListMap.forEach { (_, list) ->
                    list.remove(serviceClass)
                }

                Log.d("NotificationService", "broadcast received: $state, $serviceName")

                // ✅ Now add to the new state
                val list = stateToServiceClassListMap.getOrPut(state) { mutableListOf() }
                if (serviceClass !in list) {
                    list.add(serviceClass)
                }

                refreshNotification()
            }
        }
    }

    private fun refreshNotification() {
        Log.d("NotificationService", "Refreshing notification")
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildCustomExpandedNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("NotificationService", "onDestroy")
        // Mark all services as "Stop" again on shutdown
        val stopList = serviceClassToNameMap.keys.toMutableList()
        stateToServiceClassListMap.clear()
        stateToServiceClassListMap["Stop"] = stopList

        refreshNotification()
        unregisterReceiver(serviceStateReceiver)
    }

}
