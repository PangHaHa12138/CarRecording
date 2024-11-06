package com.gu.gl.lib.opengl.draw.base

import android.opengl.GLES20


class SimpleDrawItem(vertexArray: VertexArray) : BaseDrawItem(vertexArray) {
    override fun draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
    }
}