package com.gu.opengles.camerarecorder.mvp.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.log
import com.gu.opengles.camerarecorder.R
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract.ActionErrorType
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract.ActionErrorType.*
import com.gu.opengles.camerarecorder.mvp.MainActivity2
import com.gu.opengles.camerarecorder.mvp.model.repo.VideoDetail
import com.gu.opengles.camerarecorder.mvp.view.widget.BottomSheetFrameLayout
import com.gu.opengles.camerarecorder.mvp.view.widget.MyRoundImageView
import com.gu.opengles.camerarecorder.mvp.view.widget.VideoStateView

class MainFragment : Fragment(), CameraRecordContract.View, OnClickListener {

    private var presenter: CameraRecordContract.Presenter? = null
    private var gls: GLSurfaceView? = null
    private var previewBtn: MyRoundImageView? = null
    private var cancelBtn: FloatingActionButton? = null
    private var timerTv: TextView? = null
    private var rv: RecyclerView? = null
    private var adapter: VideoListAdapter? = null
    private var stateView: VideoStateView? = null
    private var drawerLayout: DrawerLayout? = null
    private var finBtn: Button? = null
    private var bottomSheetCloseBtn: Button? = null
    private var title: TextView? = null
    private var videoView: VideoView? = null
    private var menuOpenBtn: Button? = null
    private var rotateScreenBtn: Button? = null


    private var bst: BottomSheetFrameLayout? = null

    companion object {
        fun newInstance() = MainFragment()
        const val TAG = "MainFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val parentView = inflater.inflate(R.layout.main_fragment, container, false)
        initView(parentView)
        presenter?.initAnim(cancelBtn!!, timerTv!!, isScreenPortrait())
        log("MainFragment--onCreateView", LOGLEVEL.HIGHEST)
        initDrawerLayout(parentView)
        return parentView
    }

    override fun onResume() {
        super.onResume()
        log("MainFragment--onResume", LOGLEVEL.HIGHEST)
        presenter?.start()
        initTranslation()
    }

    override fun onStop() {
        super.onStop()
        presenter?.stop()
        log("MainFragment-onStop", LOGLEVEL.HIGHEST)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        log("MainFragment-onDestroyView", LOGLEVEL.HIGHEST)
        doClear()
    }

    override fun menuAndBottomSheetClosed(): Boolean {
        if (isMenuOpen()) {
            closeMenu()
            return false
        }
        if (isShowBottomSheet()) {
            dismissBottomSheet()
            return false
        }
        return true
    }

    override fun doClear() {
        bst?.release()
        drawerLayout?.removeDrawerListener(this)
        adapter?.release()
        presenter?.release()
        presenter = null
//        clearFlag = true
    }


    override fun setPresenter(presenter: CameraRecordContract.Presenter) {
        this.presenter = presenter
    }

    override fun onDestroy() {
        super.onDestroy()
        log("MainFragment-onDestroy", LOGLEVEL.HIGHEST)
    }

    override fun getVideoView(): VideoView? {
        return videoView
    }


    override fun showBottomSheet() {
        bst?.openBottomSheet()
    }

    override fun dismissBottomSheet() {
        stopPlayingVideo()
        bst?.closeBottomSheet()
    }

    override fun isShowBottomSheet(): Boolean {
        return bst?.isOpen() == true
    }

    override fun updateVideoTitle(fileName: String) {
        title?.text = fileName
    }

    private fun initView(view: View) {
        gls = view.findViewById(R.id.gls)
        configGLSurface(gls!!, presenter!!.getRender())
        stateView = view.findViewById(R.id.stateView)
        previewBtn = view.findViewById(R.id.previewAndStopBtn)
        cancelBtn = view.findViewById(R.id.cancelBtn)
        timerTv = view.findViewById(R.id.timerTv)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        val deleteBtn = view.findViewById<Button>(R.id.deleteBtn)
        val playBtn = view.findViewById<Button>(R.id.playBtn)
        finBtn = view.findViewById(R.id.arrowBackBtn)
        bottomSheetCloseBtn = view.findViewById(R.id.finBtn_bottom_sheet)
        menuOpenBtn = view.findViewById(R.id.menuOpenBtn)
        val menuCloseBtn = view.findViewById<Button>(R.id.menuCloseBtn)
        rv = view.findViewById(R.id.rv)
        rv?.layoutManager = LinearLayoutManager(requireContext().applicationContext)
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.dividerInsetStart = requireContext().resources.getDimension(R.dimen.DIVIDER_INSET_START).toInt()
        divider.dividerInsetEnd = requireContext().resources.getDimension(R.dimen.DIVIDER_INSET_END).toInt()

        rv?.addItemDecoration(divider)
        previewBtn?.setOnClickListener(this)
        cancelBtn?.setOnClickListener(this)
        finBtn?.setOnClickListener(this)
        bottomSheetCloseBtn?.setOnClickListener(this)
        saveBtn?.setOnClickListener(this)
        deleteBtn?.setOnClickListener(this)
        playBtn?.setOnClickListener(this)
        menuOpenBtn?.setOnClickListener(this)
        menuCloseBtn?.setOnClickListener(this)
        rotateScreenBtn = view.findViewById(R.id.screenChangeBtn)
        rotateScreenBtn?.setOnClickListener(this)
        initBottomView(view)
    }

    private fun initDrawerLayout(parentView: View) {
        drawerLayout = parentView as DrawerLayout
        drawerLayout!!.addDrawerListener(this)
        drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    @SuppressLint("SetTextI18n")
    override fun updateTimer(sec: Int) {
        requireActivity().runOnUiThread {
            timerTv?.text = "${if ((sec / 60) < 10) "0${sec / 60}" else "${sec / 60}"}:${if ((sec % 60) < 10) "0${sec % 60}" else "${sec % 60}"}"
        }
    }

    override fun hideBackGroundWidget() {
        finBtn?.visibility = INVISIBLE
        cancelBtn?.visibility = INVISIBLE
        previewBtn?.visibility = INVISIBLE
        menuOpenBtn?.visibility = INVISIBLE
        rotateScreenBtn?.visibility = INVISIBLE
    }

    override fun showBackGroundWidget() {
        finBtn?.visibility = VISIBLE
        previewBtn?.visibility = VISIBLE
        cancelBtn?.visibility = VISIBLE
        menuOpenBtn?.visibility = VISIBLE
        rotateScreenBtn?.visibility = VISIBLE
    }

    override fun onDrawerClosed(drawerView: View) {
        if (presenter?.waitingCloseDrawer() == true) {
            showBottomSheet()
            presenter?.setWaitingCloseDrawer(false)
        }
    }

    override fun onBottomSheetOpen() {
        presenter?.loadVideo()
        hideBackGroundWidget()
    }

    override fun onBottomSheetClosed() {
        showBackGroundWidget()
    }

    private fun initBottomView(parent: View) {
        bst = parent.findViewById(R.id.bst)
        bst?.setCallback(this)
        //
        val videoViewParent = parent.findViewById<FrameLayout>(R.id.videoView_parent)
        title = parent.findViewById(R.id.title)
        videoView = addVideoView(videoViewParent)
        videoView?.setMediaController(MediaController(requireContext()))
    }

    private fun addVideoView(parent: ViewGroup): VideoView {
        //tmd该死的VideoView会造成context内存泄漏，不能xml引入，手动add，使用applicationContext
        val videoView = VideoView(requireContext().applicationContext)
        //必须设置，才能让videoView中的surfaceView显示在最上层；否则用来预览的视频GlSurfaceView会有一直显示在videoView上面，造成videoView被遮盖
        videoView.setZOrderOnTop(true)
        val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        lp.gravity = Gravity.CENTER
        parent.addView(videoView, lp)
        return videoView
    }

    private fun stopPlayingVideo() {
        videoView?.stopPlayback()
    }

    @Suppress("deprecation")
    override fun onClick(v: View) {
        presenter!!.vibrate()
        when (v.id) {
            R.id.previewAndStopBtn -> {
                presenter?.userPressedPreviewAndRecordBtn()
            }

            R.id.cancelBtn -> {
                presenter!!.userStopPreviewing()
            }

            R.id.saveBtn -> {
                presenter!!.saveVideoLockState()
            }

            R.id.deleteBtn -> {
                presenter?.userDeleteItems()
            }

            R.id.playBtn -> {
                presenter?.userPlayVideo()
            }

            R.id.arrowBackBtn -> {
                (requireActivity() as MainActivity2).onBackPressed()
            }

            R.id.menuOpenBtn -> {
                openMenu()
            }

            R.id.menuCloseBtn -> {
                closeMenu()
            }

            R.id.finBtn_bottom_sheet -> {
                dismissBottomSheet()
            }

            R.id.screenChangeBtn -> {
                changeScreenOrientation()
            }
        }
    }

    override fun isScreenPortrait(): Boolean {
        return (context as Activity).requestedOrientation == SCREEN_ORIENTATION_PORTRAIT
    }

    override fun changeScreenOrientation() {
        (context as Activity).requestedOrientation = if (isScreenPortrait()) SCREEN_ORIENTATION_LANDSCAPE else SCREEN_ORIENTATION_PORTRAIT
    }

    private fun initTranslation() {
        cancelBtn?.translationX = 0f
        timerTv?.translationX = 0f
        cancelBtn?.translationY = 0f
        timerTv?.translationY = 0f
    }

    override fun isMenuOpen(): Boolean {
        return drawerLayout!!.isDrawerOpen(GravityCompat.END)
    }

    override fun closeMenu() {
        drawerLayout?.closeDrawer(GravityCompat.END)
    }

    override fun openMenu() {
        drawerLayout?.openDrawer(GravityCompat.END)
    }

    override fun configGLSurface(gls: GLSurfaceView, render: GLSurfaceView.Renderer) {
        gls.apply {
            setEGLContextClientVersion(2)
            setEGLConfigChooser(false)
            setRenderer(render)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
    }

    override fun requestNewFrame() {
        gls?.requestRender()
    }

    override fun showVideoList(data: MutableList<VideoDetail>) {
        adapter = VideoListAdapter(requireContext(), data)
        rv?.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun refreshData(index: Int) {
        if (index == -1) rv?.adapter!!.notifyDataSetChanged()
        else {
            rv?.adapter!!.notifyItemChanged(index)
        }
    }

    override fun changeState(state: CameraRecordContract.View.State) {
        when (state) {
            CameraRecordContract.View.State.PREVIEWING -> {
                stateView?.visibility = VISIBLE
                stateView?.changeState(VideoStateView.State.PREVIEWING)
                previewBtn?.setImageLevel(1)
            }

            CameraRecordContract.View.State.RECORDING -> {
                stateView?.changeState(VideoStateView.State.RECORDING)
                previewBtn?.setImageLevel(2)
            }

            else -> {
                stateView?.changeState(VideoStateView.State.IDL)
                stateView?.visibility = INVISIBLE
                previewBtn?.setImageLevel(0)
            }
        }
    }

    override fun showErrorToast(type: ActionErrorType) {
        when (type) {
            DELETE_ITEM_RECORDING -> {
                Toast.makeText(context, "您选择删除的视频正在录制中，请先暂停，再尝试删除", Toast.LENGTH_SHORT).show()
            }

            PLAY_ITEM_RECORDING -> {
                Toast.makeText(context, "该视频正在录制中，无法播放，请先暂停录制", Toast.LENGTH_SHORT).show()
            }

            PLAY_MULTI_ITEM -> {
                Toast.makeText(context, "只能选择一个视频播放", Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(context, "请先选择一个视频", Toast.LENGTH_SHORT).show()
            }
        }
    }
}