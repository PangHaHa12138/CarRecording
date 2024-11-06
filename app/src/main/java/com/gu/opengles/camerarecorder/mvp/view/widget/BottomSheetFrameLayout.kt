package com.gu.opengles.camerarecorder.mvp.view.widget

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.gu.opengles.camerarecorder.R

class BottomSheetFrameLayout : FrameLayout {
    private lateinit var bottomSheet: FrameLayout
    private var state = MenuState.CLOSED
    private val anim = ValueAnimator().also { it.duration = 100 }
    private var openRate = 1f
    private var callback: BottomSheetCallback? = null
    private var mCurrentTop = 0
    private var firstTime = true
    private var height = 0

    interface BottomSheetCallback {
        fun onBottomSheetClosed()
        fun onBottomSheetOpen()
    }

    enum class MenuState {
        OPEN, CLOSED, ANIM
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        isChildrenDrawingOrderEnabled = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        bottomSheet = findViewById(R.id.myBottomSheet)
        anim.addUpdateListener { animation ->
            val dy = animation.animatedValue as Int - mCurrentTop
            mCurrentTop = animation.animatedValue as Int
            bottomSheet.offsetTopAndBottom(dy)
        }

        anim.addListener(object : DefaultAnimListener() {

            override fun onAnimationEnd(animation: Animator) {
                if (state == MenuState.OPEN) {
                    mCurrentTop = 0
                    callback?.onBottomSheetOpen()
                } else if (state == MenuState.CLOSED) {
                    mCurrentTop = height
                    callback?.onBottomSheetClosed()
                }
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        height = MeasureSpec.getSize(heightMeasureSpec)
        if (firstTime) {
            mCurrentTop = height
            firstTime = false
        }
        val wm = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val hm = MeasureSpec.makeMeasureSpec((height * openRate).toInt(), MeasureSpec.EXACTLY)
        bottomSheet.measure(wm, hm)
        setMeasuredDimension(width, 2 * height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        bottomSheet.layout(left, mCurrentTop, right, mCurrentTop + height)
    }

    fun setCallback(callback: BottomSheetCallback) {
        this.callback = callback
    }

    fun openBottomSheet() {
        anim.setIntValues(height, 0)
        anim.start()
        state = MenuState.OPEN
    }

    fun closeBottomSheet() {
        anim.setIntValues(0, height)
        anim.start()
        state = MenuState.CLOSED
    }

    fun isOpen() = state == MenuState.OPEN
    fun isClosed() = state == MenuState.CLOSED

    fun release() {
        callback = null
        anim.removeAllListeners()
        anim.removeAllUpdateListeners()
    }
}

abstract class DefaultAnimListener : AnimatorListener {
    override fun onAnimationStart(animation: Animator) {
    }

    override fun onAnimationCancel(animation: Animator) {
    }

    override fun onAnimationRepeat(animation: Animator) {
    }
}