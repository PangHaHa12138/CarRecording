package com.gu.gl.lib.opengl.draw.base

import android.opengl.GLES20

//透明效果
class TransparentBackgroundDrawItem(vertexArray: VertexArray) : BaseDrawItem(vertexArray) {

    override fun draw() {
        //透明水印效果
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        GLES20.glDisable(GLES20.GL_BLEND)
    }
}