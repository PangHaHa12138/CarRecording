package com.gu.opengles.camerarecorder.mvp.model.render.utils

import android.graphics.Paint
import android.graphics.PointF
import java.lang.Float.max
import java.math.BigDecimal
import java.math.RoundingMode

class TextMeasureHelper {
    private val tempSize = PointF()
    private val paintHelper = PaintHelper()

    //计算多行文字borderSize
    // 每行高度叠加，计算总高度，宽度取每行最大值，同时记录每行起始y坐标
    fun measureBorderSizeMultiRows(paints: Array<Paint>, rows: Array<String>, size: PointF, startY: Array<Float>) {
        measureBorderSize(paints, rows, size, startY, false)
    }

    fun measureBorderSizeMultiColumns(paints: Array<Paint>, columns: Array<String>, size: PointF, startX: Array<Float>, padding: Float = 0f) {
        measureBorderSize(paints, columns, size, startX, true, padding)
    }

    private fun measureBorderSize(paints: Array<Paint>, texts: Array<String>, size: PointF, start: Array<Float>, isMultiCols: Boolean, padding: Float = 0f) {
        size.x = padding
        size.y = padding
        texts.forEachIndexed { index, s ->
            measureTextSizeWithPaddingNoReturn(paints[index], tempSize, s)
            if (isMultiCols) {
                size.y = max(size.y, tempSize.y + padding)
                start[index] = size.x
                size.x += tempSize.x
            } else {
                size.x = max(size.x, tempSize.x + padding)
                start[index] = size.y
                size.y += tempSize.y
            }
        }
        size.x += padding
        size.y += padding
    }

    private fun measureTextSizeWithPaddingNoReturn(textPaint: Paint, size: PointF, text: String) {
        size.x = textPaint.measureText(text)
        size.y = paintHelper.measureHeightByPaint(textPaint)
    }

    fun measureRate(borderSize: PointF, rateSize: PointF, width: Int, height: Int) {
        rateSize.x = borderSize.x / width * 2
        rateSize.y = borderSize.y / height * 2
    }

    fun getFormatFloat(f: Float, n: Int): Float {
        val bd = BigDecimal(f.toDouble())
        //设置位数
        val roundingMode = RoundingMode.HALF_UP //表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
        return bd.setScale(n, roundingMode).toFloat()
    }

}