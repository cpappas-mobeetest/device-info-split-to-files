package com.mobeetest.worker.viewModels

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.mobeetest.worker.sharedPreferences.permissions.SharedPrefsManager

class PermissionViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    // εσωτερικό mutable state
    private val _playSound = mutableStateOf(
        SharedPrefsManager.getPlaySound(context)
    )

    // αυτό εκθέτουμε στο UI
    val playSound: State<Boolean> = _playSound

    fun setPlaySound(enabled: Boolean) {
        _playSound.value = enabled
        SharedPrefsManager.setPlaySound(context, enabled)
    }

    fun getPlaySound():Boolean{
        return _playSound.value
    }
}