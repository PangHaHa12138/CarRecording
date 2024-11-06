package com.gu.gl.lib.record

import android.content.Context
import android.opengl.EGLContext
import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.Semaphore


//控制openGLRecorder状态
//必须被创建在render的onSurfaceCreated中，这样保证可以从eglContext共享数据，否则通过textureId找不到对象
//eglContext来自GLSurfaceView，绘制的数据textureId才能传给mediacodec
class GLRecorder(
    context: Context, eglContext: EGLContext, @Volatile private var callback: StateCallback? = null, width: Int, height: Int, bitRate: Int, videoRepoPath: String
) {
    private val codec: GLCodec
    private val mHandler: Handler
    private val sema: Semaphore = Semaphore(0)

    @Volatile
    private var start: Boolean = false

    init {
        codec = GLCodec(context, eglContext, width, height, bitRate, videoRepoPath)
        mHandler = Handler(HandlerThread("codec-gl").apply { start() }.looper)
    }

    fun getCurrentFilePath() = codec.getCodecFilePath()
    fun getCurrentFileName() = codec.getCodecFileName()

    fun start() {
        if (start) return
        mHandler.post {
            codec.start()
            //让env绑定到一个线程
            codec.envBindThread()
            start = true
            callback?.onRecorderStart()
        }
    }

    fun stop(doCallback: Boolean, needRefresh: Boolean) {
        if (!start) return
        start = false
        mHandler.post {
            codec.stop()
            if (doCallback) callback?.onRecorderStop(needRefresh)
        }
    }

    fun release() {
        mHandler.post {
            if (start) codec.stop()
            codec.release()
            callback = null
            sema.release()
            mHandler.looper.quitSafely()
        }
        while (callback != null) {
            //等待callback=null，即release完成
            sema.acquire()
        }
    }

    //被GLSurfaceView的render线程调用
    fun postFrame(sourceTextureId: Int, timestamp: Long) {
        if (!start) return
        mHandler.post {
            codec.drawSourceFrameOnMediaCodec(sourceTextureId, timestamp)
        }
    }

    interface StateCallback {
        fun onRecorderStart()
        fun onRecorderStop(needRefresh: Boolean)
    }

}