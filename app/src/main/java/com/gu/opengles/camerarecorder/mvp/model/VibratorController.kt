package com.gu.opengles.camerarecorder.mvp.model

import android.content.Context
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity

class VibratorController(context: Context) {

    private val vibrator: Vibrator

    init {
        @Suppress("DEPRECATION")
        vibrator = context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
    }

    @Suppress("DEPRECATION")
    fun vibrate() {
        vibrator.vibrate(60)
    }
}