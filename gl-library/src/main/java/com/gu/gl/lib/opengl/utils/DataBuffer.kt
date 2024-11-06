package com.gu.gl.lib.opengl.utils

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

//在这个项目中没有使用，顶点赋值的另一种方式
class DataBuffer(vertexData: FloatArray, textureData: FloatArray) {
    private var vboId = -1

    companion object {
        const val BYTES_PER_FLOAT = 4
        private const val COORDS_PER_VERTEX = 3

    }

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer


    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexData)
        vertexBuffer.position(0)
        textureBuffer = ByteBuffer.allocateDirect(textureData.size * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureData)
        textureBuffer.position(0)
        createBufferFromArray(vertexData.size * 4, textureData.size * 4)
    }

    private fun createBufferFromArray(vertexByteSize: Int, textureByteSize: Int) {
        // 1. 创建VBO
        val vbos = IntArray(1)
        GLES20.glGenBuffers(1, vbos, 0)
        vboId = vbos[0]
        // 2. 绑定VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        // 3. 分配VBO需要的缓存大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexByteSize + textureByteSize, null, GLES20.GL_STATIC_DRAW)
        // 4. 为VBO设置顶点数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexByteSize, vertexBuffer)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexByteSize, textureByteSize, textureBuffer)
        // 5. 解绑VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    fun setVertexAttribPointer(positionLocation: Int, byteOffset: Int) {
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        GLES20.glVertexAttribPointer(positionLocation, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * 4, byteOffset)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }
}