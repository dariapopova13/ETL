package com.university.itis.emotionstestinglibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import java.util.*
import java.util.concurrent.Semaphore


class VideoRecorder(private val activity: Activity) {
    
    
    var getVideoSize: Size? = null
    var cameraCharacteristics: CameraCharacteristics? = null
    var frontCameraId: String? = null
    private var isInit: Boolean = false
    private var isRecordingVideo: Boolean = false
    private val textureView: TextureView
    private var cameraDevice: CameraDevice? = null
    private var previewSession: CameraCaptureSession? = null
    private var videoSize: Size? = null
    private var mediaRecorder: MediaRecorder? = null
    private val manager: CameraManager
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val cameraOpenCloseLock = Semaphore(1)
    private var sensorOrientation: Int? = null
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
        }
        
        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture,
                                                 width: Int, height: Int) {
        }
        
        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            return true
        }
        
        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
            if (!isRecordingVideo) {
                startRecordingVideo()
            }
        }
        
    }
    
    init {
        this.manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
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
        cameraDevice = null
        mediaRecorder?.release()
        mediaRecorder = null
    }
    
    private fun closePreviewSession() {
            previewSession?.close()
            previewSession = null
    }
    
    private fun stopRecordingVideo() {
        previewSession?.stopRepeating()
        mediaRecorder?.stop()
        sendVideoToServer(nextVideoAbsolutePath, activity, currentTime)
    }
    
    private fun resetMediaRecorder() {
        isRecordingVideo = false
        mediaRecorder?.reset()
        nextVideoAbsolutePath = null
    }
    
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
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
        
        
        val characteristics = getCameraCharacteristics(manager)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        
        videoSize = chooseVideoSize(map?.getOutputSizes(MediaRecorder::class.java) as Array<Size>)
        
        val orientation = activity.resources.configuration.orientation
        mediaRecorder = MediaRecorder()
        if (hasAppPermissions(activity))
            manager.openCamera(getFrontCameraId(manager), stateCallback, backgroundHandler)
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
        mediaRecorder?.setVideoSize(videoSize!!.width, videoSize!!.height)
        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        val rotation = activity.windowManager.defaultDisplay.rotation
        when (sensorOrientation) {
            SENSOR_ORIENTATION_DEFAULT_DEGREES -> mediaRecorder?.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation))
            SENSOR_ORIENTATION_INVERSE_DEGREES -> mediaRecorder?.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation))
        }
        mediaRecorder?.prepare()
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
    
    fun chooseVideoSize(choices: Array<Size>): Size {
        if (videoSize == null) {
            for (size in choices) {
                if (size.width == size.height * 4 / 3 && size.width <= 1080) {
                    return size
                }
            }
            videoSize = choices[choices.size - 1]
        }
        return videoSize as Size
    }
    
    fun createTextureView(activity: Activity): TextureView {
        val textureView = TextureView(activity)
        textureView.layoutParams = FrameLayout.LayoutParams(1, 1)
        val viewGroup = (activity
                .findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        viewGroup.addView(textureView)
        return textureView
    }
    
    fun getFrontCameraId(manager: CameraManager): String? {
        if (frontCameraId == null) {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val orientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (orientation == CameraCharacteristics.LENS_FACING_FRONT)
                    frontCameraId = cameraId
            }
        }
        return frontCameraId
        
    }
    
    fun getCameraCharacteristics(manager: CameraManager): CameraCharacteristics {
        if (cameraCharacteristics == null) {
            cameraCharacteristics = manager.getCameraCharacteristics(getFrontCameraId(manager)!!)
        }
        return cameraCharacteristics!!
    }
    
    companion object {
        
        private val SENSOR_ORIENTATION_DEFAULT_DEGREES = 90
        private val SENSOR_ORIENTATION_INVERSE_DEGREES = 270
        private val DEFAULT_ORIENTATIONS = SparseIntArray()
        private val INVERSE_ORIENTATIONS = SparseIntArray()
        
        init {
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90)
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0)
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270)
            DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
        
        init {
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270)
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180)
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90)
            INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0)
        }
    }
}

class CompareSizesByArea : Comparator<Size> {
    
    override fun compare(lhs: Size, rhs: Size): Int {
        return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }
}


