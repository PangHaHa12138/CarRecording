package com.gu.opengles.camerarecorder.mvp.model

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.gu.opengles.camerarecorder.R

class SoundController(val context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(3).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build()).build()
    private var soundId1: Int = 0
    private var soundId2: Int = 0
    private var soundId3: Int = 0

    fun loadSound() {
        soundId1 = soundPool.load(context, R.raw.preview, 1)
        soundId2 = soundPool.load(context, R.raw.record, 1)
        soundId3 = soundPool.load(context, R.raw.cancel, 1)
    }

    fun playSound1() {
        playSound(soundId1)
    }

    fun playSound2() {
        playSound(soundId2)
    }

    fun playSound3() {
        playSound(soundId3)
    }

    private fun playSound(soundId: Int) {
        soundPool.play(soundId, 0.3f, 0.3f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}