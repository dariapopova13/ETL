package com.university.itis.emotionstestinglibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.university.itis.emotionstestinglibrary.ETL.manager
import com.university.itis.emotionstestinglibrary.ETL.sensorOrientation
import com.university.itis.emotionstestinglibrary.ETL.videoSize
import com.university.itis.emotionstestinglibrary.Sensors.DEFAULT_ORIENTATIONS
import com.university.itis.emotionstestinglibrary.Sensors.INVERSE_ORIENTATIONS
import com.university.itis.emotionstestinglibrary.Sensors.SENSOR_ORIENTATION_DEFAULT_DEGREES
import com.university.itis.emotionstestinglibrary.Sensors.SENSOR_ORIENTATION_INVERSE_DEGREES
import java.util.*
import java.util.concurrent.Semaphore


class VideoRecorder(private val activity: Activity) {
    
    
    private var isInit: Boolean = false
    private var isRecordingVideo: Boolean = false
    private val textureView: TextureView
    private var cameraDevice: CameraDevice? = null
    private var previewSession: CameraCaptureSession? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val cameraOpenCloseLock = Semaphore(1)
    private var mediaRecorder: MediaRecorder? = null
    private var nextVideoAbsolutePath: String? = null
    private var previewBuilder: CaptureRequest.Builder? = null
    private val stateCallback = object : CameraDevice.StateCallback() {
        
        override fun onOpened(cameraDevice: CameraDevice) {
            this@VideoRecorder.cameraDevice = cameraDevice
            startPreview()
            cameraOpenCloseLock.release()
        }
        
        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@VideoRecorder.cameraDevice = null
        }
        
        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@VideoRecorder.cameraDevice = null
        }
        
    }
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        
        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture,
                                               width: Int, height: Int) {
            openCamera(width, height)
//
        }
        
        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture,
                                                 width: Int, height: Int) {
        }
        
        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            surfaceTexture.release()
            return true
        }
        
        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
            if (!isRecordingVideo) {
                startRecordingVideo()
            }
        }
        
    }
    
    init {
        textureView = createTextureView(activity)
    }
    
    fun startRecording() {
        requestAppPermissions(activity)
        if (!hasAppPermissions(activity)) return
        startBackgroundThread()
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
            startRecordingVideo()
        }
        isInit = true
    }
    
    fun stopRecording() {
        if (!isInit) return
        if (isRecordingVideo) {
            try {
                stopRecordingVideo()
            } catch (e: RuntimeException) {
                deleteVideoFile(nextVideoAbsolutePath)
            }
        }
        resetMediaRecorder()
        closeCamera()
        stopBackgroundThread()
    }
    
    private fun closeCamera() {
        cameraOpenCloseLock.acquire()
        cameraDevice?.close()
        mediaRecorder?.release()
        cameraDevice = null
        mediaRecorder = null
    }
    
    private fun closePreviewSession() {
        try {
            previewSession?.close()
            previewSession = null
        } catch (e: IllegalStateException) {
        }
    }
    
    private fun stopRecordingVideo() {
        try {
            previewSession?.stopRepeating()
            previewSession?.abortCaptures()
        } catch (e: CameraAccessException) {
        }
        mediaRecorder?.stop()
        sendVideoToServer(nextVideoAbsolutePath, activity, currentTime)
    }
    
    private fun resetMediaRecorder() {
        isRecordingVideo = false
        mediaRecorder?.reset()
        nextVideoAbsolutePath = null
    }
    
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread(activity::class.java.name)
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper)
    }
    
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        backgroundThread?.join()
        backgroundThread = null
        backgroundHandler = null
    }
    
    @SuppressLint("MissingPermission")
    private fun openCamera(width: Int, height: Int) {
        if (activity.isFinishing) return
        val orientation = activity.resources.configuration.orientation
        mediaRecorder = MediaRecorder()
        if (hasAppPermissions(activity))
            manager.openCamera(ETL.frontCameraId, stateCallback, backgroundHandler)
    }
    
    private fun startPreview() {
        if (!textureView.isAvailable) {
            return
        }
        closePreviewSession()
        val texture = textureView.surfaceTexture
        
        val previewSurface = Surface(texture)
        previewBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        with(previewBuilder) {
            this?.addTarget(previewSurface)
        }
        cameraDevice?.createCaptureSession(listOf<Surface>(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        previewSession = session
                        try {
                            updatePreview()
                        } catch (e: RuntimeException) {
                        }
                    }
                    
                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                }, backgroundHandler)
    }
    
    private fun setUpMediaRecorder() {
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        if (nextVideoAbsolutePath == null || nextVideoAbsolutePath!!.isEmpty()) {
            nextVideoAbsolutePath = getVideoFilePath(activity)
        }
        mediaRecorder?.setOutputFile(nextVideoAbsolutePath)
        mediaRecorder?.setVideoEncodingBitRate(10000000)
        mediaRecorder?.setVideoFrameRate(30)
        mediaRecorder?.setVideoSize(videoSize.width, videoSize.height)
        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        val rotation = activity.windowManager.defaultDisplay.rotation
        when (sensorOrientation) {
            SENSOR_ORIENTATION_DEFAULT_DEGREES -> mediaRecorder?.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation))
            SENSOR_ORIENTATION_INVERSE_DEGREES -> mediaRecorder?.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation))
        }
        mediaRecorder?.prepare()
    }
    
    
    private fun updatePreview() {
        setUpCaptureRequestBuilder(previewBuilder)
        val thread = HandlerThread("CameraPreview")
        thread.start()
        try {
            previewSession?.setRepeatingRequest(previewBuilder?.build(), null, null)
        } catch (e: CameraAccessException) {
        }
    }
    
    private fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder?) {
        builder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
    }
    
    
    private fun startRecordingVideo() {
        if (!textureView.isAvailable) return
        closePreviewSession()
        setUpMediaRecorder()
        val texture = textureView.surfaceTexture
        previewBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        
        val surfaces = ArrayList<Surface?>()
        val previewSurface = Surface(texture)
        surfaces.add(previewSurface)
        previewBuilder?.addTarget(previewSurface)
        
        val recorderSurface = mediaRecorder?.surface
        surfaces.add(recorderSurface)
        previewBuilder?.addTarget(recorderSurface)
        
        cameraDevice?.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                previewSession = cameraCaptureSession
                updatePreview()
                mediaRecorder?.start()
                
                isRecordingVideo = true
            }
            
            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {}
        }, backgroundHandler)
    }
    
    private fun createTextureView(activity: Activity): TextureView {
        val textureView = TextureView(activity)
        textureView.layoutParams = FrameLayout.LayoutParams(1, 1)
        val viewGroup = (activity
                .findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        viewGroup.addView(textureView)
        return textureView
    }
}

class CompareSizesByArea : Comparator<Size> {
    
    override fun compare(lhs: Size, rhs: Size): Int {
        return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }
}


