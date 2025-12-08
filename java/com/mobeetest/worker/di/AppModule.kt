package com.mobeetest.worker.di

import android.content.Context
import android.net.wifi.WifiManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    // Παροχή WifiManager για το DeviceInfoViewModel
    single<WifiManager> {
        val context: Context = androidContext()
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }
}
