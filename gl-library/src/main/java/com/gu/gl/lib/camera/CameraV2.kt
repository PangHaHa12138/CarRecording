package com.gu.gl.lib.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraDevice.StateCallback
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.log
import java.util.*
import kotlin.math.max
import kotlin.math.min

class CameraV2 {
    private var mCameraDevice: CameraDevice? = null
    private var mCameraId: String? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mCaptureRequestBuilder: CaptureRequest.Builder? = null
    private var mCaptureRequest: CaptureRequest? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null
    private var mCameraHandler: Handler? = null
    private var mCameraThread: HandlerThread? = null

    private fun startCameraThread() {
        mCameraThread = HandlerThread("Camera-Thread")
        mCameraThread?.start()
        mCameraHandler = Handler(mCameraThread!!.looper)
    }

    private fun stopHandlerThread() {
        log("结束mCameraThread线程", LOGLEVEL.HIGHEST)
        mCameraThread?.quitSafely()
    }

    private fun findCameraId(context: Context): Boolean {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id!!)
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {//LENS_FACING_FRONT
//                    mPreviewSize =
//                        Size(getDimension(context, R.dimen.GL_WIDTH), getDimension(context, R.dimen.GL_HEIGHT))
//                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(SurfaceTexture::class.java).forEach {//
//                        log("合法的 preview size : ${it.width} x ${it.height}", LOGLEVEL.HIGHEST)
//                    }
                    mCameraId = id
                    return true
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return false
    }

    fun releaseCamera() {
        if (mCameraDevice != null) {
            mCameraDevice!!.close()
            mCameraDevice = null
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(context: Context, callback: () -> Unit) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.openCamera(mCameraId!!, object : StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                mCameraDevice = camera
                startPreview()
                callback()
            }

            override fun onDisconnected(camera: CameraDevice) {
            }

            override fun onError(camera: CameraDevice, error: Int) {
                mCameraDevice = null
            }

            override fun onClosed(camera: CameraDevice) {
                super.onClosed(camera)
                log("openCamera-- onClosed! thread is ${Thread.currentThread().name}", LOGLEVEL.HIGHEST)
                mCameraDevice = null
                stopHandlerThread()
            }
        }, mCameraHandler) //修改到mCameraHandler
    }


    //打开相机，同时开始预览
    fun cameraOpenAndPreview(context: Context, texture: SurfaceTexture, size: Size, onPreviewSuc: (Boolean) -> Unit) {
        if (findCameraId(context)) {
            if (mCameraDevice == null) {
                setPreviewTexture(texture, size)
                startCameraThread()//只有第一次启动相机时，才启动线程
                openCamera(context) {
                    onPreviewSuc(false)
                }
            } else {
                startPreview()
                onPreviewSuc(true)
            }

        } else {
            log("打开摄像头失败！", LOGLEVEL.HIGHEST)
        }
    }

    private fun setPreviewTexture(surfaceTexture: SurfaceTexture?, surfaceTextureSize: Size) {
        mSurfaceTexture = surfaceTexture
        //必须是（1280x960）不能是960x1280
        //land方向时,GlSurfaceView宽高1280x720，surfaceTure的buffersize比例1280x720
        //portrait方向，GlSurfaceView宽高是960x1280，surfaceTexture的buffer宽高也必须是1280x960。否则图像变形
        // surfaceTure的bufferSize比例，必须是(大的 x 小的)
        mSurfaceTexture!!.setDefaultBufferSize(max(surfaceTextureSize.width, surfaceTextureSize.height), min(surfaceTextureSize.width, surfaceTextureSize.height))
    }

    @Suppress("deprecation")
    fun startPreview() {
        if (mSurfaceTexture == null) {
            try {
                throw Exception("at first ,please set the value of mSurfaceTexture")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val surface = Surface(mSurfaceTexture)
        try {
            mCaptureRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)//TEMPLATE_PREVIEW
            mCaptureRequestBuilder!!.addTarget(surface)
            mCameraDevice!!.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        mCaptureRequest = mCaptureRequestBuilder!!.build()
                        mCameraCaptureSession = session
                        mCameraCaptureSession!!.setRepeatingRequest(mCaptureRequest!!, null, null)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                }

                override fun onClosed(session: CameraCaptureSession) {
                    super.onClosed(session)
                    log("onClosed!!---thread is ${Thread.currentThread().name}", LOGLEVEL.HIGHEST)
                }
            }, null) //修改mCameraHandler
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun stopPreview(isOnUiThread: Boolean) {
        if (isOnUiThread) {
            stopPreviewSelf()
        } else {
            mCameraHandler?.post {
                stopPreviewSelf()
            }
        }
    }

    private fun stopPreviewSelf() {
        try {
            mCameraCaptureSession!!.stopRepeating()
            mCameraCaptureSession!!.abortCaptures()
            mCameraCaptureSession!!.close()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }
}
