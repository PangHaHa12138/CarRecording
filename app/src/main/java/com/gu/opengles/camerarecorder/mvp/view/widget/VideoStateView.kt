package com.gu.opengles.camerarecorder.mvp.view.widget

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.RESTART
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.log
import com.gu.opengles.camerarecorder.R
import kotlin.math.abs
import kotlin.math.max

class VideoStateView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var state: State = State.IDL
    private var dotView: DotView
    private var text: CenterLineTextView

    init {
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL
        setPadding(10, 10, 10, 10)
        setBackgroundResource(R.drawable.stateview_bg)
        dotView = DotView(State.IDL, context, attrs)
        text = CenterLineTextView(State.IDL, context, attrs)
        addViewInLayout(dotView, 0, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT), true)
        addViewInLayout(text, 1, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT), true)
        visibility = INVISIBLE
    }

    enum class State(val text: String) {
        IDL("IDL"), PREVIEWING("PRR"), RECORDING("REC")
    }

    fun changeState(state: State) {
        if (this.state != state) {
            this.state = state
            dotView.changeState(state)
            text.changeState(state)
        }
    }
}


private class DotView(state: VideoStateView.State, context: Context, attrs: AttributeSet?) : StateItemView(state, context, attrs) {
    private val paintGreen: Paint = Paint().apply {
        color = Color.GREEN
        isAntiAlias = true
    }
    private val paintRed: Paint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }

    private val centerX = PADDING + RADIUS * 1f
    private val centerY = centerX
    private val r = RADIUS * 1f
    private val w = PADDING * 2 + RADIUS * 2
    private val h = w
    private val animation = ValueAnimator.ofFloat(1f, 0f).also {
        it.duration = 2000
        it.repeatMode = RESTART
        it.repeatCount = INFINITE
        it.addUpdateListener { anim ->
            this.alpha = anim.animatedValue as Float
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : this(VideoStateView.State.IDL, context, attrs)

    companion object {
        const val PADDING = 10
        const val RADIUS = 20
    }

    override fun refreshView() {
        postInvalidate()
        if (state == VideoStateView.State.RECORDING) animation.start()
        else animation.cancel()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        log("DotView--onDraw", LOGLEVEL.HIGHEST)
        if (state == VideoStateView.State.PREVIEWING) {
            canvas?.drawCircle(centerX, centerY, r, paintGreen)
        } else if (state == VideoStateView.State.RECORDING) {
            canvas?.drawCircle(centerX, centerY, r, paintRed)
        }
    }

    fun stopAnim() {
        if (animation.isRunning) {
            animation.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnim()
        animation.removeAllListeners()
    }
}

private class CenterLineTextView(state: VideoStateView.State, context: Context, attrs: AttributeSet?) : StateItemView(state, context, attrs) {

    private var textPaint: Paint = Paint().apply {
        textSize = context.resources.getDimension(R.dimen.STATE_TEXT_SIZE)
        typeface = Typeface.createFromAsset(context.assets, "SignPainter.ttc")
        color = Color.WHITE
    }

    val maxSize: PointF = getMaxText(textPaint)//提前获取文字最大长度，避免改变文字时size变化，造成需要requestLayout

    constructor(context: Context, attrs: AttributeSet?) : this(VideoStateView.State.IDL, context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val w = (maxSize.x + 0.5f).toInt()
        val h = (maxSize.y + 0.5f).toInt()
        setMeasuredDimension(w, h)
    }

    private fun getMaxText(paint: Paint): PointF {
        val size1 = simpleMeasureText(paint, VideoStateView.State.IDL.text)
        val size2 = simpleMeasureText(paint, VideoStateView.State.PREVIEWING.text)
        val size3 = simpleMeasureText(paint, VideoStateView.State.RECORDING.text)
        return PointF(max(max(size1.x, size2.x), size3.x) + 5, size1.y)
    }

    fun simpleMeasureText(paint: Paint, text: String): PointF {
        val width = paint.measureText(text)
        val fontMetrics = paint.fontMetrics
        val height = abs(fontMetrics.ascent) + abs(fontMetrics.descent)
        return PointF(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val fontMetrics = textPaint.fontMetrics
        //由于全是大写，所以ascent~baseline就是border,原ascent向下移动剩余空间的一半，就是新baseline.
        // ascent、top负
        // bottom、descent正
        val baseline = -fontMetrics.ascent + fontMetrics.descent / 2
        canvas.drawText(state.text, 0f, baseline, textPaint)
    }
}

private abstract class StateItemView(var state: VideoStateView.State, context: Context, attrs: AttributeSet?) : View(context, attrs) {

    open fun changeState(state: VideoStateView.State) {
        this.state = state
        refreshView()
    }

    open fun refreshView() {
        postInvalidate()
    }
}
