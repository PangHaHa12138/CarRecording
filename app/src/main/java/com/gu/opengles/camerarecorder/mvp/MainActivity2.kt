package com.gu.opengles.camerarecorder.mvp

import android.Manifest
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.log
import com.gu.opengles.camerarecorder.R
import com.gu.opengles.camerarecorder.mvp.presenter.MainPresenter
import com.gu.opengles.camerarecorder.mvp.utils.PermissionUtil
import com.gu.opengles.camerarecorder.mvp.view.MainFragment

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("MainActivity2-onCreate!", LOGLEVEL.LIFECYCLE)
        try2HideStatusBar()
        keepScreenOn()
        checkPermission()
        setContentView(R.layout.activity_main)
        startRecordFragment()
    }

    //如果用户选择了横屏，隐藏statusbar
    @Suppress("deprecation")
    private fun try2HideStatusBar() {
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    //屏幕常亮
    private fun keepScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startRecordFragment() {
        var mainFragment = supportFragmentManager.findFragmentByTag(MainFragment.TAG)
        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container1, mainFragment, MainFragment.TAG).commit()
        }
        MainPresenter(this).bindView(mainFragment as CameraRecordContract.View)
    }

    private fun checkPermission() {
        val list = listOf(Manifest.permission.CAMERA)
        PermissionUtil.checkPermission(this, list)
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag(MainFragment.TAG) as MainFragment
        if (fragment.menuAndBottomSheetClosed()) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        log("MainActivity2 onDestroy", LOGLEVEL.LIFECYCLE)
    }
}