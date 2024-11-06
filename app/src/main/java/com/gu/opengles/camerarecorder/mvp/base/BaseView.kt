package com.gu.opengles.camerarecorder.mvp.base

interface BaseView<out T : BasePresenter> {
    fun setPresenter(presenter:  @UnsafeVariance T)
}