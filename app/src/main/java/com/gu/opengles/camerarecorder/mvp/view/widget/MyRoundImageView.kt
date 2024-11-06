package com.gu.opengles.camerarecorder.mvp.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.log
import com.gu.opengles.camerarecorder.R
import java.lang.Integer.min


class MyRoundImageView : AppCompatImageView {
    companion object {
        private const val DEFAULT_RATE = 0.2f
    }

    private var paddingRate = DEFAULT_RATE
    private var roundBg: GradientDrawable? = null
    private var srcID: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        scaleType = ScaleType.FIT_XY
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyRoundImageView)
        paddingRate = typedArray.getFloat(R.styleable.MyRoundImageView_inset_rate, DEFAULT_RATE)
        val color = typedArray.getColor(R.styleable.MyRoundImageView_inset_bg_color, Color.BLUE)
        srcID = typedArray.getResourceId(R.styleable.MyRoundImageView_imgSrc, 0)
        typedArray.recycle()
        roundBg = GradientDrawable().also { it.setColor(color) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        log("MyRoundImageView--onMeasure", LOGLEVEL.HIGHEST)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        log("MyRoundImageView--onLayout", LOGLEVEL.HIGHEST)
        val size = (min(width, height) * (1 - 2 * paddingRate)).toInt()
        val paddingLeft = (width - size) / 2
        val paddingTop = (height - size) / 2
        setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop)
        roundBg?.also {
            it.cornerRadius = width / 2f
            background = roundBg
        }
        if (srcID != 0) {
            setImageResource(srcID)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        log("MyRoundImageView--onDraw", LOGLEVEL.HIGHEST)
    }
}


