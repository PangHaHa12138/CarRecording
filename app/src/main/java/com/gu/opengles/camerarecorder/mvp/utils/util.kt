package com.gu.opengles.camerarecorder.mvp.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.Log
import androidx.core.app.ActivityCompat
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.log
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat


fun File.getFormatFileSize(): String {
    val number = length() / 1024 / 1024f
    val format = DecimalFormat("#.#")
    format.roundingMode = RoundingMode.FLOOR
    return format.format(number) + "MB"
}


class PermissionUtil {
    companion object {
        fun checkPermission(activity: Activity, permissions: List<String>, onSuccess: (() -> Unit)? = null) {
            val pList = ArrayList<String>()
            permissions.forEach { permission ->
                val res = ActivityCompat.checkSelfPermission(activity, permission)
                Log.e("PermissionUtil", "check permission!res=$res,请求权限=${permission}")
                if (res == PackageManager.PERMISSION_DENIED) {
                    pList.add(permission)
                } else {
                    log("已经获得，不用执行请求：$permission", LOGLEVEL.HIGHEST)
                    onSuccess?.invoke()
                }
            }
            if (pList.isNotEmpty()) {
                pList.forEach {
                    log("需要请求的权限:$it", LOGLEVEL.HIGHEST)
                }
                ActivityCompat.requestPermissions(activity, pList.toTypedArray(), 100)
            }
        }
    }
}



fun getSecond(): Long {
//    return Calendar.getInstance().get(Calendar.SECOND)
    return System.currentTimeMillis() / 1000
}

fun isLandScape(context: Context): Boolean {
    val mConfiguration = context.resources.configuration; //获取设置的配置信息
    val ori = mConfiguration.orientation; //获取屏幕方向
    return ori == Configuration.ORIENTATION_LANDSCAPE
}