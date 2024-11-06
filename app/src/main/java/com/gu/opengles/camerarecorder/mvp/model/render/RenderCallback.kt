package com.gu.opengles.camerarecorder.mvp.model.render

import android.graphics.SurfaceTexture
import android.opengl.EGLContext

interface RenderCallback {
    fun onSurfaceTextureReady(surfaceTexture: SurfaceTexture, eglContext: EGLContext)
    fun onGenerateTextureFrame(textureId: Int, timestamp: Long)
    fun onFinOneFrame()
}