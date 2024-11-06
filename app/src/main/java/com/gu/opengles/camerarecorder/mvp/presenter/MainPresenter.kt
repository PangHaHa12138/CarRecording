package com.gu.opengles.camerarecorder.mvp.presenter

import android.content.Context
import android.opengl.EGLContext
import android.opengl.GLSurfaceView.Renderer
import android.util.Size
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.getDimension
import com.gu.gl.lib.opengl.utils.log
import com.gu.opengles.camerarecorder.R
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract.Presenter
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract.View
import com.gu.opengles.camerarecorder.mvp.model.*
import com.gu.opengles.camerarecorder.mvp.model.repo.VideoDetail
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class MainPresenter(private val context: Context) : Presenter, CoroutineScope {

    companion object {
        const val COROUTINE_NAME = "MainPresenter"
    }

    private val job: Job = Job()
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext + job + CoroutineName(COROUTINE_NAME)
    private var view: View? = null
    private val timerRecordTask: TimerRecordTask = TimerRecordTask(this)
    private val cameraRenderTask: CameraRenderTask = CameraRenderTask(context, this)
    private val dataModel: DataModel = DataModel(context, this)

    private val previewSize = Size(
        getDimension(context, R.dimen.GL_SURFACE_VIEW_WIDTH), getDimension(context, R.dimen.GL_SURFACE_VIEW_HEIGHT)
    )
    private var animHelper: AnimHelper? = null
    private val soundController: SoundController = SoundController(context).also { it.loadSound() }
    private val vibratorController = VibratorController(context)
    private var playBtnPressed = false

    override fun vibrate() {
        vibratorController.vibrate()
    }

    override fun bindView(view: View) {
        this.view = view
        view.setPresenter(this)
    }

    override fun initAnim(view1: android.view.View, view2: android.view.View, isPortrait: Boolean) {
        animHelper = AnimHelper(view1, view2, isPortrait)
    }

    override fun waitingCloseDrawer() = playBtnPressed

    override fun setWaitingCloseDrawer(inProgress: Boolean) {
        playBtnPressed = inProgress
    }

    override fun saveVideoLockState() {
        dataModel.saveLockState()
        view?.refreshData(-1)
    }

    override fun getCurrentRecordingFileName(): String {
        return timerRecordTask.getCurrentCodecFileName()
    }

    override fun notifyCodecComplete(filePath: String, needRefresh: Boolean) {
        //run on work thread
        dataModel.updateComplete(filePath)
        if (needRefresh) {
            launch(Dispatchers.Main) {
                view?.refreshData(dataModel.size() - 1)
            }
        }
    }

    override fun notifyCodecStart() {
        dataModel.addAndDelete()
        launch(Dispatchers.Main) {
            view?.refreshData(-1)
        }
    }

    override fun notifySecChanged(sec: Int) {
        log("更新时间", LOGLEVEL.NOR)
        view?.updateTimer(sec)
    }

    override fun notifySecReset() {
        log("时间reset", LOGLEVEL.HIGHEST)
        view?.updateTimer(0)
    }

    override fun getRender(): Renderer {
        return cameraRenderTask.getRender()
    }

    override fun isPreviewing(): Boolean {
        return cameraRenderTask.isPreviewing()
    }

    override fun isRecording(): Boolean {
        return timerRecordTask.isRecording()
    }

    override fun userPressedPreviewAndRecordBtn() {
        log("onclick previewAndStopBtn", LOGLEVEL.HIGHEST)
        if (!isPreviewing()) userStartPreviewing()
        else if (!isRecording()) userStartRecording()
        else userStopRecording()
    }

    override fun userStartPreviewing() {
        animHelper?.startPreviewingAnim {
            log("tryOpenCameraAndPreview相机！", LOGLEVEL.HIGHEST)
            playPreviewSound()
            tryOpenCameraAndPreview()
        }
    }

    override fun userStopPreviewing() {
        animHelper?.stopPreviewingAnim {
            log("onclick cancelBtn", LOGLEVEL.HIGHEST)
            playCancelSound()
            if (isPreviewing()) {
                stopPreview()
            }
        }
    }

    override fun userStartRecording() {
        animHelper?.startRecordingAnim {
            playRecordSound()
            log("开始录制！thread is :${Thread.currentThread().name}", LOGLEVEL.HIGHEST)
            startRecord()
        }
    }

    override fun userStopRecording() {
        animHelper?.stopRecordingAnim {
            log("btn click 结束录制！", LOGLEVEL.HIGHEST)
            playCancelSound()
            stopRecord()
            stopPreview()
        }
    }

    //总流程为：先关闭menu,menu动画结束后，再弹出bottomSheet,弹出动画结束后，再开始播放
    override fun userPlayVideo() {
        val result = dataModel.choosePlayItem()
        if (result.isSuc()) {
            setWaitingCloseDrawer(true)
            view?.closeMenu()
        } else view?.showErrorToast(result.errorType!!)
    }

    override fun userDeleteItems() {
        val chooseRes = dataModel.chooseDeleteItem()
        if (chooseRes.isSuc()) view?.refreshData(-1)
        else view?.showErrorToast(chooseRes.errorType!!)
    }

    private fun tryOpenCameraAndPreview() {
        cameraRenderTask.tryCameraPreview(this, previewSize) {
            view?.requestNewFrame()
            view?.changeState(View.State.PREVIEWING)
        }
    }

    private fun stopPreview() {
        view?.changeState(View.State.IDL)
        cameraRenderTask.stopPreview(false)
    }

    private fun startRecord() {
        view?.changeState(View.State.RECORDING)
        timerRecordTask.startTimerTask()
    }

    private fun stopRecord() {
        timerRecordTask.stopTimerTask(need2Callback = true, need2Refresh = true)
    }

    //call on MainFragment.onResume()
    override fun start() {
        view?.changeState(View.State.IDL)
        dataModel.loadVideos(this, context)
    }

    //call on MainFragment.onStop()
    override fun stop() {
        if (isRecording()) {
            timerRecordTask.stopTimerTask(need2Callback = false, need2Refresh = false)
        }
        if (isPreviewing()) {
            cameraRenderTask.stopPreview(true)
        }
        dataModel.release()
    }

    override fun onLocalDataReady(data: MutableList<VideoDetail>) {
        view?.showVideoList(data)
    }

    override fun onShareEGLContext(eglContext: EGLContext) {
        timerRecordTask.initRecorder(context, eglContext, previewSize.width, previewSize.height, dataModel.getVideoDirPath())
    }

    override fun onGenerateFrame(textureId: Int, timestamp: Long) {
        timerRecordTask.codecFrame(textureId, timestamp)
    }

    override fun requestNewFrame() {
        view?.requestNewFrame()
    }

    override fun release() {
        timerRecordTask.release()
        cameraRenderTask.release()
        soundController.release()
        cancel()
        view = null
    }

    override fun playVideo(path: String) {
        view?.getVideoView()?.setVideoPath(path)
        view?.getVideoView()?.start()
    }

    override fun loadVideo() {
        val selectedItem = dataModel.getPlayItem()
        if (selectedItem.recording) selectedItem.path = timerRecordTask.getCurrentCodecFilePath()
        view?.updateVideoTitle(getVideoName(selectedItem.path))
        playVideo(selectedItem.path)
        dataModel.clearPlaySelected()
        dataModel.resetSelectItems()
        view?.refreshData(-1)
    }

    override fun playPreviewSound() {
        soundController.playSound1()
    }

    override fun playRecordSound() {
        soundController.playSound2()
    }

    override fun playCancelSound() {
        soundController.playSound3()
    }

    private fun getVideoName(path: String): String {
        val arrays = path.split('/')
        return arrays[arrays.lastIndex]
    }
}