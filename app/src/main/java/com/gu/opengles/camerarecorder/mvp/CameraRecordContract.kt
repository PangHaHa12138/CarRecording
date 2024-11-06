package com.gu.opengles.camerarecorder.mvp

import android.animation.Animator
import android.opengl.EGLContext
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.view.View
import android.widget.VideoView
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.gu.opengles.camerarecorder.mvp.base.BasePresenter
import com.gu.opengles.camerarecorder.mvp.base.BaseView
import com.gu.opengles.camerarecorder.mvp.model.repo.VideoDetail
import com.gu.opengles.camerarecorder.mvp.view.widget.BottomSheetFrameLayout

interface CameraRecordContract {
    interface View : BaseView<Presenter>, DefaultDrawerListener, BottomSheetFrameLayout.BottomSheetCallback {

        fun isScreenPortrait(): Boolean
        fun changeScreenOrientation()
        fun getVideoView(): VideoView?
        fun updateVideoTitle(fileName: String)
        fun showBottomSheet()
        fun dismissBottomSheet()
        fun isShowBottomSheet(): Boolean
        fun configGLSurface(gls: GLSurfaceView, render: Renderer)
        fun requestNewFrame()
        fun showVideoList(data: MutableList<VideoDetail>)
        fun refreshData(index: Int)
        fun changeState(state: State)
        fun isMenuOpen(): Boolean
        fun closeMenu()
        fun openMenu()
        fun doClear()
        fun hideBackGroundWidget()
        fun showBackGroundWidget()
        fun updateTimer(sec: Int)
        fun menuAndBottomSheetClosed(): Boolean

        fun showErrorToast(type: ActionErrorType)


        enum class State {
            PREVIEWING, RECORDING, IDL
        }
    }

    interface Presenter : BasePresenter, TimerChangeCallback {
        fun bindView(view: View)
        fun vibrate()
        fun initAnim(view1: android.view.View, view2: android.view.View, isPortrait: Boolean)
        fun saveVideoLockState()
        fun getCurrentRecordingFileName(): String
        fun onLocalDataReady(data: MutableList<VideoDetail>)
        fun onShareEGLContext(eglContext: EGLContext)
        fun onGenerateFrame(textureId: Int, timestamp: Long)
        fun requestNewFrame()

        fun userPressedPreviewAndRecordBtn()
        fun userStartPreviewing()
        fun userStartRecording()
        fun userStopPreviewing()
        fun userStopRecording()
        fun userPlayVideo()
        fun userDeleteItems()

        fun getRender(): Renderer
        fun isPreviewing(): Boolean
        fun isRecording(): Boolean
        fun notifyCodecComplete(filePath: String, needRefresh: Boolean)
        fun notifyCodecStart()
        fun loadVideo()
        fun playVideo(path: String)
        fun waitingCloseDrawer(): Boolean
        fun setWaitingCloseDrawer(inProgress: Boolean)

        fun playPreviewSound()
        fun playRecordSound()
        fun playCancelSound()
    }

    interface TimerChangeCallback {
        fun notifySecChanged(sec: Int)
        fun notifySecReset()
    }

    enum class ActionErrorType {
        DELETE_ITEM_RECORDING, PLAY_ITEM_RECORDING, PLAY_MULTI_ITEM, NO_SELECTED
    }

    class TaskResult private constructor(var res: Result?, var errorType: ActionErrorType? = null) {

        companion object {
            fun createSuccess(): TaskResult {
                return TaskResult(Result.SUC)
            }

            fun createFail(errorType: ActionErrorType): TaskResult {
                return TaskResult(Result.FAL, errorType)
            }
        }

        fun isSuc() = res == Result.SUC

        enum class Result {
            SUC, FAL
        }
    }
}

//DrawerListener ,no use default implement
interface DefaultDrawerListener : DrawerListener {
    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
    override fun onDrawerOpened(drawerView: View) {}
    override fun onDrawerStateChanged(newState: Int) {}
}

//DefaultAnimListener ,no use default implement
abstract class DefaultAnimListener : Animator.AnimatorListener {
    override fun onAnimationStart(animation: Animator) {
    }

    override fun onAnimationCancel(animation: Animator) {
    }

    override fun onAnimationRepeat(animation: Animator) {
    }
}