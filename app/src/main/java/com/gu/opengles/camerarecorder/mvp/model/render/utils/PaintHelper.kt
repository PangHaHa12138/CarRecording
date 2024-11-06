package com.gu.opengles.camerarecorder.mvp.model.render.utils

import android.graphics.Paint
import android.graphics.Typeface

class PaintHelper {

    fun createTextPaint(textSizePx: Int, textColor: Int): Paint {
        val textPaint = Paint()
        textPaint.textSize = textSizePx.toFloat()
        textPaint.color = textColor
        return textPaint
    }

    fun createTextPaint(textSizePx: Int, textColor: Int, typeface: Typeface): Paint {
        val textPaint = Paint()
        textPaint.textSize = textSizePx.toFloat()
        textPaint.color = textColor
        textPaint.typeface = typeface
        return textPaint
    }

    fun createTextPaint(textSizePx: Int): Paint {
        val textPaint = Paint()
        textPaint.textSize = textSizePx.toFloat()
        return textPaint
    }

    fun measureHeightByPaint(paint: Paint): Float {
        val fontMetrics = paint.fontMetrics
        return fontMetrics.bottom - fontMetrics.top
    }
}