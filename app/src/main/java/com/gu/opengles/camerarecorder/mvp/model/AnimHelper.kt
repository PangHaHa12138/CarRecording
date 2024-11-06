package com.gu.opengles.camerarecorder.mvp.model

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.gu.opengles.camerarecorder.mvp.DefaultAnimListener

class AnimHelper(view1: View, view2: View, isPortrait: Boolean) {

    private var animShowCancelBtn = AnimatorSet()
    private var animHideCancelBtnAndShowTimer = AnimatorSet()
    private var animHideCancelBtn = AnimatorSet()
    private var animHideTimerTv = AnimatorSet()

    init {
        val animArray = Array<ObjectAnimator>(8) { index: Int ->
            when (index) {
                0 -> ObjectAnimator.ofFloat(view1, "alpha", 0f, 1.0f)
                1 -> ObjectAnimator.ofFloat(view1, if (isPortrait) "translationX" else "translationY", 0f, -300f)
                2 -> ObjectAnimator.ofFloat(view1, "alpha", 1f, 0f)
                3 -> ObjectAnimator.ofFloat(view1, if (isPortrait) "translationX" else "translationY", -300f, 0f)
                4 -> ObjectAnimator.ofFloat(view2, "alpha", 0f, 1.0f)
                5 -> ObjectAnimator.ofFloat(view2, if (isPortrait) "translationX" else "translationY", 0f, 300f)
                6 -> ObjectAnimator.ofFloat(view2, "alpha", 1f, 0f)
                else -> ObjectAnimator.ofFloat(view2, if (isPortrait) "translationX" else "translationY", 300f, 0f)
            }
        }
        setAnimsDuration(animArray)
        animShowCancelBtn.playTogether(animArray[0], animArray[1])
        animHideCancelBtn.playTogether(animArray[2], animArray[3])
        animHideCancelBtnAndShowTimer.playTogether(animArray[2], animArray[3], animArray[4], animArray[5])
        animHideTimerTv.playTogether(animArray[6], animArray[7])
    }

    fun stopPreviewingAnim(endAction: () -> Unit) {
        after(animHideCancelBtn, endAction)
    }

    fun startPreviewingAnim(endAction: () -> Unit) {
        after(animShowCancelBtn, endAction)
    }

    fun startRecordingAnim(endAction: () -> Unit) {
        after(animHideCancelBtnAndShowTimer, endAction)
    }

    fun stopRecordingAnim(endAction: () -> Unit) {
        after(animHideTimerTv, endAction)
    }

    private fun setAnimsDuration(array: Array<ObjectAnimator>) {
        array.forEach {
            it.duration = 500L
        }
    }

    private fun after(animation: Animator, endAction: () -> Unit) {
        startAnimAndDoEndAction(animation, endAction)
    }

    private inline fun startAnimAndDoEndAction(anim: Animator, crossinline endAction: () -> Unit) {
        anim.addListener(object : DefaultAnimListener() {
            override fun onAnimationEnd(animation: Animator) {
                endAction()
                anim.removeListener(this)
            }
        })
        anim.start()
    }
}