package com.gu.opengles.camerarecorder.mvp.model

import android.content.Context
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract.ActionErrorType.*
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract.Presenter
import com.gu.opengles.camerarecorder.mvp.CameraRecordContract.TaskResult
import com.gu.opengles.camerarecorder.mvp.model.repo.SharePreferenceController
import com.gu.opengles.camerarecorder.mvp.model.repo.VideoDetail
import com.gu.opengles.camerarecorder.mvp.model.repo.VideosRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataModel(private var context: Context, private var presenter: Presenter?) {
    private var repo: VideosRepo = VideosRepo(context)
    private var playItem: VideoDetail? = null

    fun getPlayItem() = playItem!!
    fun clearPlaySelected() {
        playItem = null
    }

    //获取本地外部存储目录路径
    fun getVideoDirPath() = repo.getDirPath()

    fun loadVideos(coroutineScope: CoroutineScope, context: Context) {
        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                repo.loadData(context)
            }
            presenter?.onLocalDataReady(repo.getData()!!)
        }
    }

    fun choosePlayItem(): TaskResult {
        if (multiSelected()) {
            return TaskResult.createFail(PLAY_MULTI_ITEM)
        }
        val selectItem = findSelectItem()
        if (selectItem == null) {
            return TaskResult.createFail(NO_SELECTED)
        } else if (selectItem.recording) {
            return TaskResult.createFail(PLAY_ITEM_RECORDING)
        }
        playItem = selectItem
        return TaskResult.createSuccess()
    }

    fun chooseDeleteItem(): TaskResult {
        if (recordingItemSelected()) {
            return TaskResult.createFail(DELETE_ITEM_RECORDING)
        } else if (findSelectItem() == null) {
            return TaskResult.createFail(NO_SELECTED)
        }
        deleteSelected()
        return TaskResult.createSuccess()
    }

    private fun recordingItemSelected(): Boolean {
        return repo.recordingItemSelected()
    }

    private fun multiSelected(): Boolean {
        return repo.countSelected() > 1
    }

    private fun findSelectItem(): VideoDetail? {
        return repo.findSelectedItem()
    }

    fun resetSelectItems() {
        repo.dataStateReset { it.selected = false }
    }

    fun saveLockState() {
        if (isSelectRecordingFile()) {
            val name = presenter?.getCurrentRecordingFileName()
            repo.updateRecordingItemName(name!!)
        }
        SharePreferenceController.changeLockedVideos(context, repo.getSelectItemNameList())
        repo.dataStateReset()
    }

    private fun isSelectRecordingFile(): Boolean {
        return repo.containFile { it.recording and it.selected }
    }

    //返回删除的个数
    fun addAndDelete(): Int {
        return repo.addAndDelete()
    }

    fun size() = repo.currentSize()

    private fun deleteSelected() {
        repo.deleteSelectedFiles(context)
    }

    fun updateComplete(path: String) {
        repo.updateComplete(path)
    }

    fun release() {
        repo.release()
    }
}