package com.gu.gl.lib.opengl.draw.base

import android.opengl.GLES20
import android.util.Log

//draw的数据全都绘制到了framebuffer上,framebuffer绑定了纹理outputTextureId,
// 向framebuffer绘制数据，等于向outputTextureId的纹理绘制数据，拿到的outputTextureId是绘制结果，给mediacodec可以录制
class FrameBuffer(w: Int, h: Int) {

    private var frameBufferId = -1
    var outputTextureId = -1
    private val textureId = IntArray(1)

    init {
        createFrameBuffer(w, h)
    }

    private fun createFrameBuffer(width: Int, height: Int) {
        // 1. 创建FBO
        val fbos = IntArray(1)
        GLES20.glGenFramebuffers(1, fbos, 0)
        frameBufferId = fbos[0]
        // 2. 绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)

        // 3. 创建FBO纹理
        // 创建纹理
        GLES20.glGenTextures(1, textureId, 0)
        outputTextureId = textureId[0]
        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, outputTextureId)
        // 环绕（超出纹理坐标范围）  （s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        // 过滤（纹理像素映射到坐标点）  （缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // 4. 把纹理绑定到FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, outputTextureId, 0)

        // 5. 设置FBO分配内存大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)

        // 6. 检测是否绑定从成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("TAG", "glFramebufferTexture2D error")
        }
        // 7. 解绑纹理和FBO
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    fun bindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)
    }

    fun unBindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    fun release() {
        GLES20.glDeleteTextures(1, textureId, 0)
    }
}