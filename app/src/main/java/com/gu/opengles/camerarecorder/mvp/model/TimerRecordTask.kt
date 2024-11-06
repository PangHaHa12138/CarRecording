package com.gu.opengles.camerarecorder.mvp.model

import android.content.Context
import android.opengl.EGLContext
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.log
import com.gu.gl.lib.record.GLRecorder
import com.gu.opengles.camerarecorder.R
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract.Presenter
import java.util.*
import kotlin.concurrent.timerTask

class TimerRecordTask(private var presenter: Presenter?) : GLRecorder.StateCallback {
    private var recorder: GLRecorder? = null
    private var secTimer: Timer? = null

    @Volatile
    private var recording = false
    private var recorderTimePeriod = 0
    private var sec = 0

    fun isRecording(): Boolean {
        return recording
    }

    fun initRecorder(context: Context, eglContext: EGLContext, width: Int, height: Int, dirPath: String) {
        val bitRate = context.resources.getInteger(R.integer.BIT_RATE)
        recorder = GLRecorder(context, eglContext, this, width, height, bitRate, dirPath)
        recorderTimePeriod = context.resources.getInteger(R.integer.RECORDING_DURATION)
    }

    fun startTimerTask() {
        recording = true
        sec = 0
        recorder?.start()
        Timer().apply {
            secTimer = this
            scheduleAtFixedRate(timerTask {
                sec++
                if (sec == recorderTimePeriod) {
                    recorder?.stop(true, !recording)//在录制中，就不用刷新，因为后面马上开启录制新视频，会刷新一次
                    sec = 0
                    if (recording) {
                        presenter?.notifySecReset()
                        recorder?.start()
                    }
                } else {
                    if (recording) {
                        presenter?.notifySecChanged(sec)
                    }
                }
            }, 1000, 1000)
        }
    }

    fun stopTimerTask(need2Callback: Boolean, need2Refresh: Boolean) {
        recording = false
        log("-----recording = false", LOGLEVEL.HIGHEST)
        recorder?.stop(need2Callback, need2Refresh)
        cancelTimer()
    }

    //call on GLRecorder
    override fun onRecorderStart() {
        presenter?.notifyCodecStart()
    }

    //call on GLRecorder
    //needRefresh: recycleView是否需要刷新
    override fun onRecorderStop(needRefresh: Boolean) {
        presenter?.notifyCodecComplete(recorder!!.getCurrentFilePath(), needRefresh)
    }

    fun getCurrentCodecFilePath(): String {
        return recorder!!.getCurrentFilePath()
    }

    fun codecFrame(sourceTextureId: Int, timestamp: Long) {
        recorder?.postFrame(sourceTextureId, timestamp)
    }

    fun getCurrentCodecFileName(): String {
        return recorder!!.getCurrentFileName()
    }

    fun release() {
        cancelTimer()
        recorder?.release()
        presenter = null
    }

    private fun cancelTimer() {
        secTimer?.cancel()
        secTimer = null
    }
}