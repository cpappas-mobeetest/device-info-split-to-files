package com.mobeetest.worker

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.mobeetest.worker.di.appModule
import com.mobeetest.worker.utils.features.ModeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
// TODO: Όταν φέρουμε αυτά τα modules στο νέο project, ξεσχολίασε:
// import com.mobeetest.worker.di.appModule
// import com.mobeetest.worker.util.utils.EnvSettings
// import com.mobeetest.worker.util.features.ModeManager
// import org.koin.android.ext.koin.androidContext
// import org.koin.core.context.startKoin

class MobeetestApp : Application() {

    private var enableDebugLogging: Boolean = false

    override fun onCreate() {
        super.onCreate()

        // TODO: Όταν προστεθούν ξανά τα utilities, ξεσχολίασε:
        /*
        EnvSettings.loadFromAssets(applicationContext)
        enableDebugLogging = EnvSettings
            .getOrDefault("enable_debug_logging", "false")
            .toBooleanStrictOrNull() == true
        */

        ModeManager.initialize(applicationContext)


        startKoin {
            androidContext(this@MobeetestApp)
            modules(appModule)
        }

        initializeFirebase()

        // Ενεργοποίηση Crashlytics συλλογής
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = true
    }

    private fun initializeFirebase() {
        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser?.isAnonymous == true) {
            log("Existing anonymous user found: ${auth.currentUser?.uid}")
            fetchFirebaseToken(auth)
        } else {
            signInAnonymously(auth)
        }
    }

    private fun signInAnonymously(auth: FirebaseAuth) {
        auth.signInAnonymously().addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                log("Anonymous sign-in successful")
                fetchFirebaseToken(auth)
            } else {
                log("Anonymous sign-in failed", authTask.exception)
                retrySignInAnonymously()
            }
        }
    }

    private fun retrySignInAnonymously() {
        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously().addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                log("Retry Anonymous sign-in successful")
                fetchFirebaseToken(auth)
            } else {
                log("Retry Anonymous sign-in failed", authTask.exception)
            }
        }
    }

    private fun fetchFirebaseToken(auth: FirebaseAuth) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                fcm_token = tokenTask.result ?: ""
                fcm_uuid = auth.currentUser?.uid ?: ""
                log("FCM token: $fcm_token")

                // Δένουμε τον χρήστη στο Crashlytics
                FirebaseCrashlytics.getInstance().setUserId(fcm_uuid)
            } else {
                fcm_token = ""
                log("Fetching FCM token failed", tokenTask.exception)
            }
        }
    }

    private fun log(message: String, throwable: Throwable? = null) {
        if (!enableDebugLogging) return

        if (throwable != null) {
            Log.e("FCM", message, throwable)
        } else {
            Log.d("FCM", message)
        }
    }

    companion object {
        var fcm_token: String = "N/A"
        var fcm_uuid: String = "N/A"
    }
}
