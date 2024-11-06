package com.gu.gl.lib.opengl.utils

import android.content.Context
import android.util.Log

enum class LOGLEVEL(val value: Int) {
    NOR(1), LIFECYCLE(2), HIGHEST(3)
}

var logToggle = true
fun log(str: String, level: LOGLEVEL) {
    if (logToggle && level.value > LOGLEVEL.LIFECYCLE.value) Log.e("TAG", "----$str----")
}

fun getMatrixString(matrix: FloatArray): String {
    val sb = StringBuilder("{").append('\n')
    matrix.forEachIndexed { index, fl ->
        sb.append("$fl,")
        if (index % 4 == 3) sb.append('\n')
    }
    sb.append('\n').append("}")
    return sb.toString()
}

fun getDimension(context: Context, id: Int): Int = context.resources.getDimension(id).toInt()