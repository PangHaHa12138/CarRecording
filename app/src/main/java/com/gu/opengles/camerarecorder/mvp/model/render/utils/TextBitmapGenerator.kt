package com.gu.opengles.camerarecorder.mvp.model.render.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import kotlin.math.abs

class TextBitmapGenerator {
    private val paintHelper = PaintHelper()

    /*
    params:@paints: Array<Paint>每行文字使用的不同的paint,有可能字体不同，大小不同
    params:@rows 每行文字数组
    params:@startY 每行文字的起始绘制点y，每行文字高度不一样，起始高度y不一样
    params:@size 整体宽高size
     */
    fun generateMultiRowsTextBitmap(paints: Array<Paint>, rows: Array<String>, size: PointF, startY: Array<Float>): Bitmap {
        val bitmap = Bitmap.createBitmap(size.x.toInt(), size.y.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        rows.forEachIndexed { index, s ->
            canvas.drawText(s, 0f, startY[index] + abs(paints[index].fontMetrics.top), paints[index])
        }
        return bitmap
    }

    //绘制多列不同字体文字
    fun generateMultiColsTextBitmap(colsPaints: Array<Paint>, cols: Array<String>, size: PointF, startX: Array<Float>): Bitmap {
        val bitmap = Bitmap.createBitmap(size.x.toInt(), size.y.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().also { it.color = 0x7d42a5f5 }
        canvas.drawRoundRect(0f, 0f, size.x, size.y, 10f, 10f, paint)
        cols.forEachIndexed { index, s ->
            canvas.drawText(s,
                startX[index],
                measureMoveDimen(size.y, paintHelper.measureHeightByPaint(colsPaints[index])) + abs(colsPaints[index].fontMetrics.top),
                colsPaints[index])
        }
        return bitmap
    }

    private fun measureMoveDimen(borderHeight: Float, insetBorderHeight: Float): Float {
        return (borderHeight - insetBorderHeight) / 2
    }

}