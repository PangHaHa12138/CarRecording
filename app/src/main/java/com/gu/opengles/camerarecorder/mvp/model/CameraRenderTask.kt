package com.gu.opengles.camerarecorder.mvp.model

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGLContext
import android.opengl.GLSurfaceView
import android.util.Size
import com.gu.gl.lib.camera.CameraV2
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract.Presenter
import com.gu.opengles.camerarecorder.mvp.model.render.CameraPreviewRender
import com.gu.opengles.camerarecorder.mvp.model.render.RenderCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraRenderTask(var context: Context?, private var presenter: Presenter?) : RenderCallback, SurfaceTexture.OnFrameAvailableListener {

    private val render: CameraPreviewRender = CameraPreviewRender(context!!)
    private var mSurfaceTexture: SurfaceTexture? = null
    private val camera: CameraV2 by lazy { CameraV2() }
    private var previewing = false

    init {
        render.setCallback(this)
    }

    fun getRender(): GLSurfaceView.Renderer {
        return render
    }

    fun isPreviewing(): Boolean {
        return previewing
    }

    fun tryCameraPreview(scope: CoroutineScope, size: Size, onSuccess: () -> Unit) {
        camera.cameraOpenAndPreview(context!!, mSurfaceTexture!!, size) { onMainThread ->
            previewing = true
            if (onMainThread) onSuccess()
            else scope.launch(Dispatchers.Main) { onSuccess() }
        }
    }

    fun stopPreview(isOnUiThread: Boolean) {
        previewing = false
        camera.stopPreview(isOnUiThread)
    }

    override fun onSurfaceTextureReady(surfaceTexture: SurfaceTexture, eglContext: EGLContext) {
        surfaceTexture.setOnFrameAvailableListener(this)
        mSurfaceTexture = surfaceTexture
        //call on GLSurfaceView render thread
        //传递来的eglContext用来OpenglRecorder构建EGL环境时，共享eglContext；如果不共享eglContext,你是不能通过textureID找到texture的。很关键
        presenter?.onShareEGLContext(eglContext)
    }

    override fun onGenerateTextureFrame(textureId: Int, timestamp: Long) {
        //提供完成二次绘制的textureId
        presenter?.onGenerateFrame(textureId, timestamp)
    }

    override fun onFinOneFrame() {
        mSurfaceTexture?.updateTexImage()
    }

    //由于surfaceTexture给了相机，当相机开始预览时，有新画面时，会调用该方法
    //手动让glsurfaceview调用requestRender,会调用render中的onDrawFrame
    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        if (previewing) {
            presenter?.requestNewFrame()
        }
    }

    fun release() {
        camera.releaseCamera()
        context = null
        render.clear()
        mSurfaceTexture?.setOnFrameAvailableListener(null)
        mSurfaceTexture = null
        presenter = null
    }

}