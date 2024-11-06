package com.gu.gl.lib.opengl.draw.base

abstract class BaseDrawItem(private val vertexArray: VertexArray) {

    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private const val STRIDE: Int = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * 4
    }

    open fun bindProgram(program: Program) {
        vertexArray.setVertexAttribPointer(0, program.avPosition, POSITION_COMPONENT_COUNT, STRIDE)
        vertexArray.setVertexAttribPointer(2, program.afPosition, TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE)
    }

    abstract fun draw()

    fun release() {
        vertexArray.release()
    }

}

