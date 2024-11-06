package com.gu.opengles.camerarecorder.mvp.model.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import com.gu.gl.lib.opengl.draw.base.*
import com.gu.gl.lib.opengl.draw.base.TextArrayBuilder.Companion.posLeftBottom
import com.gu.gl.lib.opengl.draw.base.TextArrayBuilder.Companion.posRightTop
import com.gu.gl.lib.opengl.utils.LOGLEVEL
import com.gu.gl.lib.opengl.utils.ShaderUtil
import com.gu.gl.lib.opengl.utils.getDimension
import com.gu.gl.lib.opengl.utils.log
import com.gu.opengles.camerarecorder.R

import com.gu.opengles.camerarecorder.mvp.utils.getSecond
import com.gu.opengles.camerarecorder.mvp.utils.isLandScape
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraPreviewRender(private var context: Context?) : Renderer {

    private val renderHelper = RenderHelper(context!!)
    private val width = getDimension(context!!, R.dimen.GL_SURFACE_VIEW_WIDTH)
    private val height = getDimension(context!!, R.dimen.GL_SURFACE_VIEW_HEIGHT)
    private var callback: RenderCallback? = null

    private var mOESTextureId = -1
    private var informationTextureId: IntArray = IntArray(1) { -1 }
    private var authorTextureId: IntArray = IntArray(1) { -1 }

    private lateinit var mSurfaceTexture: SurfaceTexture

    private var cameraPreviewItem: BaseDrawItem? = null
    private var cameraPreviewProgram: Program? = null
    private var infoItem: BaseDrawItem? = null
    private var infoItemRenderProgram: Program? = null

    private var authorItem: BaseDrawItem? = null
    private var authorItemRenderProgram: Program? = null

    private var screenItem: BaseDrawItem? = null
    private var screenProgram: Program? = null

    private lateinit var frameBuffer: FrameBuffer
    private var firstDraw = true
    private var lastSecond = -1L

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {/*
        这个mSurfaceTexture的id是mOESTextureId。
        之后把这个mSurfaceTexture绑定到相机，相机有画面会向上面绘制图像。
        相机更新画面到这个mSurfaceTexture之后,我们用这个mOESTextureId拿到mSurfaceTexture的画面，
        我们可以把他绘制到framebuffer上，之后就可以再添加一些水印纹理等，进行二次绘制
        */
        log("CameraPreviewRender onSurface--Created", LOGLEVEL.HIGHEST)
        mOESTextureId = ShaderUtil.createOESTextureObject()
        mSurfaceTexture = SurfaceTexture(mOESTextureId)
        //共享glcontext，这样从这里绘制的texture，就可以通过textureId拿到纹理
        callback?.onSurfaceTextureReady(mSurfaceTexture, EGL14.eglGetCurrentContext())
        initCameraPreviewDrawItem()
        initInformationDrawItem()
        initAuthorDrawItem()
        initScreenDrawItem()
        frameBuffer = FrameBuffer(width, height)
    }

    //相机预览
    private fun initCameraPreviewDrawItem() {
        val cameraVertexArray = SimpleBuilder.create(if (isLandScape(context!!)) vertex_normal.copyOf() else vertex_normal_portrait.copyOf()).build()
        cameraPreviewItem = SimpleDrawItem(cameraVertexArray)
        cameraPreviewProgram = Program(context!!, com.gu.gl_library.R.raw.preview_vertex_shader, com.gu.gl_library.R.raw.preview_fragment_shader)
    }

    //时间、温度、车速信息
    private fun initInformationDrawItem() {
        val rateSize = renderHelper.calculateInformationSize()
        val infoVertexArray = TextArrayBuilder.create(vertex_text.copyOf(), rateSize, renderHelper.getMarginRate(), posLeftBottom).build()
        infoItem = TransparentBackgroundDrawItem(infoVertexArray)
        infoItemRenderProgram = Program(context!!, com.gu.gl_library.R.raw.default_vertex_shader, com.gu.gl_library.R.raw.default_fragment_shader)
    }

    //作者信息
    private fun initAuthorDrawItem() {
        val rateSize = renderHelper.calculateAuthorTextureSize()
        //因为内容不变，可以提前创建authorTexture
        renderHelper.createAuthorTexture(authorTextureId)
        val authorVertexArray = TextArrayBuilder.create(vertex_text.copyOf(), rateSize, renderHelper.getMarginRate(), posRightTop).build()
        authorItem = TransparentBackgroundDrawItem(authorVertexArray)
        authorItemRenderProgram = Program(context!!, com.gu.gl_library.R.raw.default_vertex_shader, com.gu.gl_library.R.raw.default_fragment_shader)
    }

    //完整画面
    private fun initScreenDrawItem() {
        val screenVertexArray = SimpleBuilder.create(vertex_rotate.copyOf()).build()
        screenItem = SimpleDrawItem(screenVertexArray)
        screenProgram = Program(context!!, com.gu.gl_library.R.raw.default_vertex_shader, com.gu.gl_library.R.raw.default_fragment_shader)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        log("CameraPreviewRender onSurface--Changed", LOGLEVEL.HIGHEST)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (firstDraw) {
            firstDraw = false
            return
        }
        val textureId = drawOnFrameBuffer()
        drawOnScreen(textureId)
        //编码录制一帧数据
        callback?.onGenerateTextureFrame(textureId, mSurfaceTexture.timestamp)
        //通知更新一帧数据
        callback?.onFinOneFrame()
    }

    //返回一个带有水印的预览画面textureID
    private fun drawOnFrameBuffer(): Int {
        frameBuffer.bindFrameBuffer()
        drawCameraPreview()
        if (prepareInformationTexture()) {
            drawInformation(informationTextureId[0])
        }
        if (authorTextureId[0] != -1) {
            drawAuthor(authorTextureId[0])
        }
        frameBuffer.unBindFrameBuffer()
        return frameBuffer.outputTextureId
    }

    //会因为时间变化而不断调用
    private fun prepareInformationTexture(): Boolean {
        val cs = getSecond()
        if (lastSecond == cs) {
            //如果时间不变，时间戳水印不用更新，重用mWaterPrintIds[0]，直接返回
            return true
        }
        //每秒更新一次textureId
        //时间变了一秒，重新create
        lastSecond = cs
        return renderHelper.createInformationTextTure(informationTextureId)
    }

    fun setCallback(callback: RenderCallback) {
        this.callback = callback
    }

    private fun drawCameraPreview() {
        simpleDraw(cameraPreviewProgram!!, cameraPreviewItem!!, mOESTextureId, GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
    }

    private fun drawInformation(textureId: Int) {
        simpleDraw(infoItemRenderProgram!!, infoItem!!, textureId, GLES20.GL_TEXTURE_2D)
    }

    private fun drawAuthor(textureId: Int) {
        simpleDraw(authorItemRenderProgram!!, authorItem!!, textureId, GLES20.GL_TEXTURE_2D)
    }

    private fun drawOnScreen(textureId: Int) {
        simpleDraw(screenProgram!!, screenItem!!, textureId, GLES20.GL_TEXTURE_2D)
    }

    private fun simpleDraw(program: Program, drawItem: BaseDrawItem, textureId: Int, target: Int) {
        program.useProgram()
        program.setUniforms(target = target, textureId = textureId)
        drawItem.bindProgram(program)
        drawItem.draw()
    }

    fun clear() {
        frameBuffer.release()
        cameraPreviewItem?.release()
        cameraPreviewProgram?.release()
        infoItem?.release()
        infoItemRenderProgram?.release()
        authorItem?.release()
        authorItemRenderProgram?.release()
        screenItem?.release()
        screenProgram?.release()
        context = null
        callback = null
    }

}