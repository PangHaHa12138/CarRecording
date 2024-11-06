package com.gu.gl.lib.opengl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

public class EGLEnv {

    private final EGLContext mEglContext;
    private final EGLSurface mEglSurface;
    private final EGLDisplay mEglDisplay;

    public EGLEnv(EGLContext eglContext, Surface codecSurface) {
        // 获得显示窗口，作为OpenGL的绘制目标
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        // 初始化mEglDisplay
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglInitialize failed");
        }

        // 配置 属性选项
        //565 332 666 888
        int[] configAttr = {EGL14.EGL_RED_SIZE, 8, //颜色缓冲区中红色位数//8
                EGL14.EGL_GREEN_SIZE, 8,//颜色缓冲区中绿色位数//8
                EGL14.EGL_BLUE_SIZE, 8, //8
                EGL14.EGL_ALPHA_SIZE, 8,//8
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, //opengl es 2.0
                EGL14.EGL_NONE};
        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        //EGL 根据属性选择一个配置
        if (!EGL14.eglChooseConfig(mEglDisplay, configAttr, 0, configs, 0, configs.length, numConfigs, 0)) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }

        EGLConfig config = configs[0];
        /*
         * EGL上下文
         */
        int[] context_attrib_list = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
        //GLSurfaceView中的EGLContext 共享数据，共享才能纹理id拿到纹理
        mEglContext = EGL14.eglCreateContext(mEglDisplay, config, eglContext, context_attrib_list, 0);

        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }

        /*
         * 创建EGLSurface
         */
        int[] surface_attrib_list = {EGL14.EGL_NONE};
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, config, codecSurface, surface_attrib_list, 0);
        if (mEglSurface == null) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
    }

    /**
     * 绑定到当前调用的thread上
     */
    public void bindOnCurrentThread() {
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
    }

    public void swapBuffers(long timestamp) {
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, mEglSurface, timestamp);
        EGL14.eglSwapBuffers(mEglDisplay, mEglSurface);
    }

    public void release() {
        EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(mEglDisplay, mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
//        recordFilter.release();
    }
}