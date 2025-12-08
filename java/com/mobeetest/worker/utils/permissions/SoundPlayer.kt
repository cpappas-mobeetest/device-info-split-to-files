package com.mobeetest.worker.utils.permissions

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.mobeetest.worker.R

class SoundPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playSound(resourceId: Int) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("SoundPlayer", "Error playing sound", e)
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

object PermissionsStatusMp3 {
    val GRANT = R.raw.permission_granted
    val DENIED = R.raw.permission_denied
}