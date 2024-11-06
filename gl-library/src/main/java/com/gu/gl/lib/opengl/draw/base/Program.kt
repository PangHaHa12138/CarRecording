package com.gu.gl.lib.opengl.draw.base

import android.content.Context
import android.opengl.GLES20
import com.gu.gl.lib.opengl.utils.ShaderUtil

class Program(context: Context, vertexShaderId: Int, fragmentShaderId: Int) {
    private val program: Int
    private val sTexture: Int
    var avPosition: Int
    var afPosition: Int

    init {
        program = ShaderUtil.createProgram(ShaderUtil.readRawTxt(context, vertexShaderId), ShaderUtil.readRawTxt(context, fragmentShaderId))
        sTexture = GLES20.glGetUniformLocation(program, S_TEXTURE)
        avPosition = GLES20.glGetAttribLocation(program, AV_POSITION)
        afPosition = GLES20.glGetAttribLocation(program, AF_POSITION)
    }

    companion object {
        const val S_TEXTURE = "sTexture"
        const val AV_POSITION = "av_Position"
        const val AF_POSITION = "af_Position"
    }

    fun useProgram() {
        GLES20.glUseProgram(program)
    }

    //默认使用GLES20.GL_TEXTURE0单元和0
    fun setUniforms(textureUnit: Int = GLES20.GL_TEXTURE0, uniformNum: Int = 0, target: Int, textureId: Int) {
        GLES20.glActiveTexture(textureUnit)
        GLES20.glBindTexture(target, textureId)
        GLES20.glUniform1i(sTexture, uniformNum)
    }

    fun release() {
        GLES20.glDeleteProgram(program)
    }
}