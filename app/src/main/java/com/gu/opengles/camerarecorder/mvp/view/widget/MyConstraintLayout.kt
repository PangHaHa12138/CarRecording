package com.gu.opengles.camerarecorder.mvp.view.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.log

class MyConstraintLayout : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        log("MyConstraintLayout--onMeasure!", LOGLEVEL.HIGHEST)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        log("MyConstraintLayout--onLayout!", LOGLEVEL.HIGHEST)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        log("MyConstraintLayout--onDraw!", LOGLEVEL.HIGHEST)
    }
}