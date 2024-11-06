package com.gu.opengles.camerarecorder.mvp.model.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Typeface
import android.opengl.GLES20
import androidx.core.content.ContextCompat
import com.gu.gl.lib.opengl.utils.ShaderUtil
import com.gu.opengles.camerarecorder.R
import com.gu.opengles.camerarecorder.mvp.model.render.utils.PaintHelper
import com.gu.opengles.camerarecorder.mvp.model.render.utils.SimpleSensor.Companion.getDateTimeString
import com.gu.opengles.camerarecorder.mvp.model.render.utils.SimpleSensor.Companion.getTemperature
import com.gu.opengles.camerarecorder.mvp.model.render.utils.TextBitmapGenerator
import com.gu.opengles.camerarecorder.mvp.model.render.utils.TextMeasureHelper

class RenderHelper(context: Context) {
    companion object {
        val AUTHOR_TEXT = arrayOf("FHD", " 30p 8Mbps")
        val INFO_TEXT: Array<String> = arrayOf(getTemperature(), getDateTimeString())
    }

    private val width = getDimension(context, R.dimen.GL_SURFACE_VIEW_WIDTH)
    private val height = getDimension(context, R.dimen.GL_SURFACE_VIEW_HEIGHT)
    private val paintHelper = PaintHelper()
    private val measureHelper = TextMeasureHelper()

    private val textMargin = getDimension(context, R.dimen.SCREEN_TEXT_PADDING)
    private val informationTextSize = getDimension(context, R.dimen.INFO_TEXT_SIZE)
    private val authorTextSize = getDimension(context, R.dimen.AUTHOR_TEXT_SIZE)
    private val authorTextSizeSecondary = getDimension(context, R.dimen.AUTHOR_TEXT_SIZE_SECONDARY)

    private val informationTextColor = getColor(context, R.color.information_text_color)

    private val authorNameColor = getColor(context, R.color.author_text_color)
    private val authorSecondaryColor = getColor(context, R.color.author_text_color_secondary)
    private val authorTypeface = getTypeface(context, "STIXTwoText-Italic.ttf")
    private val informationTypeface = getTypeface(context, "Arial_Rounded_Bold.ttf")
    private var infoTextPaints = Array(INFO_TEXT.size) {
        paintHelper.createTextPaint(informationTextSize, informationTextColor, informationTypeface)
    }
    private var authorTextPaints = Array(AUTHOR_TEXT.size) { index ->
        if (index == 0) paintHelper.createTextPaint(authorTextSizeSecondary, authorSecondaryColor)
        else paintHelper.createTextPaint(authorTextSize, authorNameColor, authorTypeface)
    }

    private val informationRowsStartY = Array(INFO_TEXT.size) { 0f }
    private val authorColumnsStartX = Array(AUTHOR_TEXT.size) { 0f }

    private val mTextBitmapGenerator = TextBitmapGenerator()
    private val informationBorderSize = PointF()
    private val authorBorderSize = PointF()
    private val rateSize = PointF()

    fun calculateInformationSize(): PointF {
        measureHelper.measureBorderSizeMultiRows(infoTextPaints, INFO_TEXT, informationBorderSize, informationRowsStartY)
//        infoBorderSize.x = width.toFloat()//手动设置宽度等于preview width
        measureHelper.measureRate(informationBorderSize, rateSize, width, height)
        return rateSize
    }

    fun calculateAuthorTextureSize(): PointF {
        measureHelper.measureBorderSizeMultiColumns(authorTextPaints, AUTHOR_TEXT, authorBorderSize, authorColumnsStartX, 10f)
        measureHelper.measureRate(authorBorderSize, rateSize, width, height)
        return rateSize
    }

    //创建新informationTextTure,同时释放上一秒的texture
    fun createInformationTextTure(textureID: IntArray): Boolean {
        changeInformationText(getTemperature(), getDateTimeString())
        val bitmap = generatorInformationBitmap()
        //并释放上一秒的texture
        if (textureID[0] != -1) {
            GLES20.glDeleteTextures(1, textureID, 0)
        }
        //创建一个带有水印图像的texture，返回创建是否成功结果
        return ShaderUtil.createBitmapTexture(textureID, bitmap)
    }


    //只在初始化是调用一次
    // 必须先调用initAuthorTexture()对数组初始化
    fun createAuthorTexture(authorTextureId: IntArray): Boolean {
        val authorBitmap =
            mTextBitmapGenerator.generateMultiColsTextBitmap(authorTextPaints, AUTHOR_TEXT, authorBorderSize, authorColumnsStartX)
        return ShaderUtil.createBitmapTexture(authorTextureId, authorBitmap)
    }

    fun getMarginRate(): PointF {
        return PointF(measureHelper.getFormatFloat(textMargin * 2f / width, 2), measureHelper.getFormatFloat(textMargin * 2f / height, 2))
    }

    private fun changeInformationText(vararg texts: String) {
        texts.forEachIndexed { index, s ->
            INFO_TEXT[index] = s
        }
    }

    private fun generatorInformationBitmap(): Bitmap {
        return mTextBitmapGenerator.generateMultiRowsTextBitmap(infoTextPaints, INFO_TEXT, informationBorderSize, informationRowsStartY)
    }

    private fun getColor(context: Context, id: Int) = ContextCompat.getColor(context, id)
    private fun getDimension(context: Context, id: Int) = context.resources!!.getDimension(id).toInt()
    private fun getTypeface(context: Context, typefaceName: String): Typeface {
        return Typeface.createFromAsset(context.assets, typefaceName)
    }
}