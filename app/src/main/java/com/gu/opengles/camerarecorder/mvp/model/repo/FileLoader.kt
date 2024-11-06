package com.gu.opengles.camerarecorder.mvp.model.repo

import android.content.Context

import com.gu.opengles.camerarecorder.R
import java.io.File
import java.util.*


class FileLoader(context: Context) {
    private val dirPath: String

    init {
        val dirName = context.getString(R.string.repo_dir_name)
        dirPath = createFileDirIfNOExist(context, dirName).path
    }



    fun getDirPath() = dirPath

    fun getLocalFiles(): MutableList<VideoDetail> {
        val files = File(dirPath).listFiles()
        if (files.isNullOrEmpty()) return ArrayList()
        return files.filter { it.name.endsWith("mp4") }.map { VideoDetail().update(it) }.toMutableList()
    }

    private fun createFileDirIfNOExist(context: Context, dirName: String): File {

        val dir = File(context.getExternalFilesDir(null), dirName)
        if (!dir.exists()) dir.mkdir()
        return dir
    }

}