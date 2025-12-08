package com.mobeetest.worker.utils.device

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20

object HeadlessGpuInfoFetcher {

    data class GpuInfoResult(
        val vendor: String,
        val renderer: String,
        val version: String,
        val extensions: List<String>
    )

    fun fetch(): GpuInfoResult {
        try {
            val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            EGL14.eglInitialize(eglDisplay, null, 0, null, 0)

            val attribList = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_NONE
            )

            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfig = IntArray(1)
            EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfig, 0)

            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            val eglContext = EGL14.eglCreateContext(
                eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0
            )

            val surfaceAttribs = intArrayOf(
                EGL14.EGL_WIDTH, 1,
                EGL14.EGL_HEIGHT, 1,
                EGL14.EGL_NONE
            )
            val eglSurface = EGL14.eglCreatePbufferSurface(
                eglDisplay, configs[0], surfaceAttribs, 0
            )

            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

            val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
            val version = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"
            val extensionsStr = GLES20.glGetString(GLES20.GL_EXTENSIONS) ?: ""
            val extensions = extensionsStr.split(" ").filter { it.isNotBlank() }

            EGL14.eglMakeCurrent(
                eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
            )
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglTerminate(eglDisplay)

            return GpuInfoResult(vendor, renderer, version, extensions)

        } catch (_: Exception) {
            return GpuInfoResult("Unknown", "Unknown", "Unknown", emptyList())
        }
    }

}
