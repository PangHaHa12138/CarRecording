package com.gu.gl.lib.opengl.draw.base

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

val vertex_normal = floatArrayOf( // Order of coordinates: X, Y, S, T
    -1f, 1f, 0f, 0f,//左上
    -1f, -1f, 0f, 1f,//左下
    1f, -1f, 1f, 1f,//右下
    -1f, 1f, 0f, 0f,//左上
    1f, -1f, 1f, 1f,//右下
    1f, 1f, 1f, 0f//右上
)

//val vertex_normal_portrait = floatArrayOf( // Order of coordinates: X, Y, S, T
//    -1f, 1f, 1f, 1f,//左上
//    -1f, -1f, 0f, 1f,//左下
//    1f, -1f, 0f, 0f,//右下
//    -1f, 1f, 1f, 1f,//左上//////3
//    1f, -1f, 0f, 0f,//右下
//    1f, 1f, 1f, 0f//右上
//)

//val vertex_normal_portrait = floatArrayOf( // Order of coordinates: X, Y, S, T
//    -1f, 1f, 0f, 0f,//左上
//    -1f, -1f, 1f, 0f,//左下
//    1f, -1f, 1f, 1f,//右下//////1
//    -1f, 1f, 0f, 0f,//左上
//    1f, -1f, 1f, 1f,//右下
//    1f, 1f, 0f, 1f//右上
//)

//val vertex_normal_portrait = floatArrayOf( // Order of coordinates: X, Y, S, T
//    -1f, 1f, 1f, 0f,//左上
//    -1f, -1f, 0f, 0f,//左下
//    1f, -1f, 0f, 1f,//右下////2
//    -1f, 1f, 1f, 0f,//左上
//    1f, -1f, 0f, 1f,//右下
//    1f, 1f, 1f, 1f//右上
//)

val vertex_normal_portrait = floatArrayOf( // Order of coordinates: X, Y, S, T
    -1f, 1f, 0f, 1f,//左上
    -1f, -1f, 1f, 1f,//左下
    1f, -1f, 1f, 0f,//右下////4
    -1f, 1f, 0f, 1f,//左上
    1f, -1f, 1f, 0f,//右下
    1f, 1f, 0f, 0f//右上
)

//纹理旋转180度的矩阵
val vertex_rotate = floatArrayOf( // Order of coordinates: X, Y, S, T
    -1f, 1f, 0f, 1f,//左上
    -1f, -1f, 0f, 0f,//左下
    1f, -1f, 1f, 0f,//右下
    -1f, 1f, 0f, 1f,//左上
    1f, -1f, 1f, 0f,//右下
    1f, 1f, 1f, 1f//右上
)

//左边纹理水印坐标
val vertex_text = floatArrayOf( // Order of coordinates: X, Y, S, T
    -1f, 1f, 0f, 0f,//左上
    -1f, -1f, 0f, 1f,//左下
    1f, -1f, 1f, 1f,//右下
    -1f, 1f, 0f, 0f,//左上
    1f, -1f, 1f, 1f,//右下
    1f, 1f, 1f, 0f//右上
)

//val vertex_left_bottom = floatArrayOf( // Order of coordinates: X, Y, S, T
//    -1f, -0.9f, 0f, 0f,//左上
//    -1f, -1f, 0f, 1f,//左下
//    0.5f, -1f, 1f, 1f,//右下
//    -1f, -0.9f, 0f, 0f,//左上
//    0.5f, -1f, 1f, 1f,//右下
//    0.5f, -0.9f, 1f, 0f//右上
//)

class VertexArray(vertexData: FloatArray) {
    private val floatBuffer: FloatBuffer

    init {
        floatBuffer = ByteBuffer.allocateDirect(vertexData.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData)
    }

    fun setVertexAttribPointer(dataOffset: Int, attributeLocation: Int, componentCount: Int, stride: Int) {
        floatBuffer.position(dataOffset)
        GLES20.glVertexAttribPointer(attributeLocation, componentCount, GLES20.GL_FLOAT, false, stride, floatBuffer)
        GLES20.glEnableVertexAttribArray(attributeLocation)
        floatBuffer.position(0)
    }

    fun release() {
        floatBuffer.clear()
    }
}