package com.gu.gl.lib.record

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.EGLContext
import com.gu.gl.lib.opengl.EGLEnv
import com.gu.gl.lib.opengl.draw.GlTextureDraw
import com.gu.gl_library.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

//eglContext来自GLSurfaceView的EGL14.eglGetCurrentContext(),共享eglContext
//videoRepoPath视频文件存储的根目录
class GLCodec(context: Context, private val eglContext: EGLContext, width: Int, height: Int, bitRate: Int, private val videoRepoPath: String) {
    private val glDraw: GlTextureDraw
    private val format: MediaFormat
    private lateinit var mMediaCodec: MediaCodec
    private lateinit var mMuxer: MediaMuxer
    private var track = 0
    private var mLastTimeStamp: Long = 0
    private var currentFilePath: String = ""
    private var currentFileName: String = ""
    private lateinit var eglEnv: EGLEnv

    companion object {
        fun generateFileName(): String {
            val formatter = SimpleDateFormat("yyyy年MM月dd日-HH点mm分ss秒", Locale.getDefault())
            return "记录" + formatter.format(Calendar.getInstance().time) + ".mp4"
        }
    }

    init {
        glDraw = GlTextureDraw(context.applicationContext, R.raw.default_vertex_shader, R.raw.default_fragment_shader, width, height)
        format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)//MIMETYPE_VIDEO_AVC
        //format.setInteger(MediaFormat.KEY_WIDTH,1080)
        //format.setInteger(MediaFormat.KEY_HEIGHT,1920)
        //颜色空间 从 surface当中获得
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)//COLOR_FormatSurface
        //码率  比特率决定了视频的质量。一般来说，比特率越高，视频质量越好
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate) //录制的视频大小与KEY_BIT_RATE有直接关系，跟设置视频长宽尺寸无关
        //帧率  帧率决定了视频的流畅度。常见的帧率有 30 fps 和 60 fps
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        //关键帧间隔  I 帧（关键帧）间隔控制两个关键帧之间的帧数。通常设置为 1 秒内的帧数
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1 * 30) // 对于 30 fps 的视频

    }

    private fun prepare() {
        mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)////MIMETYPE_VIDEO_AVC
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)//format
        //这个surface，理解为一块等待写入数据的内存空间，我们的camera画面，都通过opengl向他上面画数据。然后mediacodec再通过他拿到数据，再进行编码写文件等操作
        val codecSurface = mMediaCodec.createInputSurface()
        //混合器 (复用器) 将编码的h.264封装为mp4
        currentFileName = generateFileName()
        currentFilePath = videoRepoPath + File.separator + currentFileName
        mMuxer = MediaMuxer(currentFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        eglEnv = EGLEnv(eglContext, codecSurface)
    }

    fun getCodecFilePath() = currentFilePath
    fun getCodecFileName() = currentFileName


    fun start() {
        prepare()
        mMediaCodec.start()
    }

    //EGL环境一定绑定到handlerThread,让env绑定到一个线程
    fun envBindThread() {
        eglEnv.bindOnCurrentThread()
    }

    //被GLSurfaceView的render线程调用
    fun drawSourceFrameOnMediaCodec(textureId: Int, timestamp: Long) {
        //录制用的opengl已经和handler的线程绑定了 ，所以需要在这个线程中使用录制的opengl
        glDraw.drawSourceTextureId(textureId)
        eglEnv.swapBuffers(timestamp)
        codecOneFrame(false)
    }

    //编码一帧数据
    @Suppress("DEPRECATION")
    private fun codecOneFrame(end: Boolean) {
        //给个结束信号
        if (end) {
            mMediaCodec.signalEndOfInputStream()
        }
        while (true) {
            //获得输出缓冲区 (编码后的数据从输出缓冲区获得)
            val bufferInfo = MediaCodec.BufferInfo()
            val encoderStatus = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000)
            //需要更多数据
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                //如果是结束那直接退出，否则继续循环
                if (!end) break
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //输出格式发生改变  第一次总会调用所以在这里开启混合器
                val newFormat = mMediaCodec.outputFormat
                track = mMuxer.addTrack(newFormat)
                mMuxer.start()
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                //可以忽略
            } else {
                //调整时间戳
                //有时候会出现异常 ： timestampUs xxx < lastTimestampUs yyy for Video track
                if (bufferInfo.presentationTimeUs <= mLastTimeStamp) {
                    bufferInfo.presentationTimeUs = mLastTimeStamp + 1000000 / 25L
                }
                mLastTimeStamp = bufferInfo.presentationTimeUs

                //正常则 encoderStatus 获得缓冲区下标
                val encodedData = mMediaCodec.getOutputBuffer(encoderStatus)
                //如果当前的buffer是配置信息，不管它 不用写出去
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    bufferInfo.size = 0
                }
                if (bufferInfo.size != 0) {
                    //设置从哪里开始读数据(读出来就是编码后的数据)
                    encodedData!!.position(bufferInfo.offset)
                    //设置能读数据的总长度
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    //写出为mp4
                    mMuxer.writeSampleData(track, encodedData, bufferInfo)
                }
                // 释放这个缓冲区，后续可以存放新的编码后的数据啦
                mMediaCodec.releaseOutputBuffer(encoderStatus, false)
                // 如果给了结束信号 signalEndOfInputStream
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    break
                }
            }
        }
    }

    fun stop() {
        codecOneFrame(true)
        mMediaCodec.stop()
        mMediaCodec.release()
        mMuxer.stop()
        mMuxer.release()
        eglEnv.release()
        //add
    }

    fun release() {
        glDraw.release()
    }
}