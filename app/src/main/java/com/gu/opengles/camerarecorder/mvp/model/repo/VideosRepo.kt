package com.gu.opengles.camerarecorder.mvp.model.repo

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import com.gu.opengles.camerarecorder.R
import com.gu.opengles.camerarecorder.mvp.utils.getFormatFileSize
import java.io.File

class VideosRepo(context: Context) {
    private val maxNum: Int = context.resources.getInteger(R.integer.FILE_MAX_NUM)
    private var data: MutableList<VideoDetail>? = null
    private var fileLoader = FileLoader(context)

    fun getDirPath() = fileLoader.getDirPath()

    fun loadData(context: Context) {
        val lockedFiles = SharePreferenceController.getLockedVideosStr(context)
        data = fileLoader.getLocalFiles().onEach { it.locked = lockedFiles.contains(it.name) }.apply { sortBy { it.name } }
    }

    private fun getSelectItemList(list: List<VideoDetail>): List<VideoDetail> {
        return list.filter { it.selected }
    }

    fun getSelectItemNameList(): List<String> {
        return getSelectItemList(data!!).map { it.name }
    }

    fun findSelectedItem() = data?.find { it.selected }

    fun containFile(filter: (item: VideoDetail) -> Boolean): Boolean {
        return data!!.any(filter)
    }

    fun currentSize() = data!!.count()

    private val dataStateResetAction = { it: VideoDetail ->
        it.selected = false
        it.locked = !it.locked
    }

    //list选中的item重置
    fun dataStateReset(action: (VideoDetail) -> Unit = dataStateResetAction) {
        data?.filter { it.selected }?.forEach { action(it) }
    }


    fun getData(): MutableList<VideoDetail>? {
        return data
    }

    fun recordingItemSelected(): Boolean {
        return data?.find { it.recording and it.selected } != null
    }

    fun countSelected(): Int {
        return data?.count { it.selected } ?: 0
    }

    fun updateRecordingItemName(name: String) {
        data?.find { it.recording }?.updateName(name)
    }

    fun updateComplete(path: String) {
        data?.find { it.recording }?.update(File(path))
    }

    fun addAndDelete(): Int {
        data!!.add(VideoDetail())
        val changedNum = calcDeleteNumbers()
        deleteUnlockedFiles(changedNum)
        return changedNum
    }

    private fun calcDeleteNumbers(): Int {
        val size = data!!.count()
        val lockSize = data!!.count { it.locked }
        return size - lockSize - maxNum
    }

    fun deleteSelectedFiles(context: Context, action: (VideoDetail) -> Unit = defaultRecycleAction) {
        getSelectItemList(data!!).takeIf { it.isNotEmpty() }?.onEach { action(it) }?.apply {
            SharePreferenceController.removeLockedVideos(context, map { it.name })
            data?.removeAll(this)
        }
    }

    //自动删除未锁定的file
    private fun deleteUnlockedFiles(deleteNum: Int, action: (VideoDetail) -> Unit = defaultRecycleAction) {
        if (deleteNum <= 0) return
        data!!.filter { !it.locked }.take(deleteNum).onEach { action(it) }.toList().apply { data!!.removeAll(this) }
    }

    /**
     * clean files and return the deleted files number
     */
    private val defaultRecycleAction: (VideoDetail) -> Unit = { item ->
        item.recycle()
        File(item.path).delete()
    }

    fun release() {
        if (data != null) {
            data?.onEach { it.recycle() }?.clear()
        }
    }
}

class VideoDetail {
    var name: String
    var path: String
    var fileSize: String
    var bitmap: Bitmap?
    var selected: Boolean = false
    var locked: Boolean = false
    var recording: Boolean = true

    init {
        name = "RECORDING"
        path = "..."
        fileSize = "0b"
        bitmap = null
    }

    fun update(file: File): VideoDetail {
        recording = false
        name = file.name
        path = file.absolutePath
        fileSize = file.getFormatFileSize()
        bitmap = getLocalVideoThumbnail(path)
        return this
    }

    fun updateName(name: String) {
        this.name = name
    }

    @Suppress("deprecation")
    fun getLocalVideoThumbnail(filePath: String?): Bitmap? {
        return ThumbnailUtils.createVideoThumbnail(filePath!!, MediaStore.Images.Thumbnails.MINI_KIND)
    }

    fun recycle() {
        if (bitmap == null) return
        if (!bitmap!!.isRecycled) bitmap!!.recycle()
    }
}