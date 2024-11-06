package com.gu.gl.lib.opengl.draw

import android.content.Context
import android.opengl.GLES20
import com.gu.gl.lib.opengl.draw.base.*

class GlTextureDraw(context: Context, vertexShaderId: Int, fragmentShaderId: Int, private val width: Int, private val height: Int) {
    private val program: Program
    private val textureItem: BaseDrawItem

    init {
        program = Program(context, vertexShaderId, fragmentShaderId)
        val vex = SimpleBuilder.create(vertex_rotate.copyOf()).build()
        textureItem = SimpleDrawItem(vex)
    }

    //绘制textureId到display
    fun drawSourceTextureId(textureId: Int) {
        GLES20.glViewport(0, 0, width, height)
        program.useProgram()
        program.setUniforms(GLES20.GL_TEXTURE1, 1, GLES20.GL_TEXTURE_2D, textureId)
        textureItem.bindProgram(program)
        textureItem.draw()
    }

    fun release() {
        program.release()
        textureItem.release()
    }

}