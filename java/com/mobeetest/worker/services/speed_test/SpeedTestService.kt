package com.mobeetest.worker.services.speed_test

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SpeedTestService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action?.uppercase() ?: intent?.getStringExtra("status")?.uppercase()

        if (action in listOf("RUN", "PAUSE", "STOP")) {
            // Προσομοίωση καθυστέρησης για αξιοπιστία
            val broadcastIntent = Intent("com.mobeetest.worker.SERVICES_STATES").apply {
                putExtra("state", action)
                putExtra("service_name", "Speed Test")
                setPackage(packageName)
            }
            sendBroadcast(broadcastIntent)
            Log.d("SpeedTestService", "Broadcast sent with action $action")
        }

        when (action) {
            "RUN","RUN_ALL" -> startJob()
            "PAUSE","PAUSE_ALL" -> pauseJob()
            "STOP","STOP_ALL" -> stopJob()
        }

        return START_STICKY
    }

    private fun startJob() {
        // TODO: Implement job start logic
    }

    private fun pauseJob() {
        // TODO: Implement job pause logic
    }

    private fun stopJob() {
        // TODO: Implement job stop logic
        stopSelf() // Optionally stop the service
    }

}
