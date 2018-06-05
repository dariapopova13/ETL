package com.university.itis.emotionstestinglibrary

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface

object ETL {
    lateinit var application: Application
    lateinit var apiKey: String
    var deleteVideos: Boolean = false
    lateinit var testId: String
    var number = 1
    var testAB = "a"
    
    lateinit var frontCameraId: String
    lateinit var cameraCharacteristics: CameraCharacteristics
    lateinit var videoSize: Size
    lateinit var manager: CameraManager
    var sensorOrientation: Int = 0
    
    fun initLib() {
        manager = application.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        getFrontCameraId()
        getCameraCharacteristics()
        sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        getVideoSize()
    }
    
    private fun getVideoSize() {
        val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        chooseVideoSize(map?.getOutputSizes(MediaRecorder::class.java) as Array<Size>)
    }
    
    private fun getCameraCharacteristics() {
        cameraCharacteristics = manager.getCameraCharacteristics(frontCameraId)
    }
    
    private fun chooseVideoSize(choices: Array<Size>) {
        for (size in choices) {
            if (size.width == size.height * 4 / 3 && size.width <= 1080) {
                videoSize = size
            }
        }
        videoSize = choices[choices.size - 1]
    }
    
    private fun getFrontCameraId() {
        for (cameraId in manager.cameraIdList) {
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val orientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
            if (orientation == CameraCharacteristics.LENS_FACING_FRONT)
                frontCameraId = cameraId
        }
    }
}

object Sensors {
    
    val SENSOR_ORIENTATION_DEFAULT_DEGREES = 90
    val SENSOR_ORIENTATION_INVERSE_DEGREES = 270
    val DEFAULT_ORIENTATIONS = SparseIntArray()
    val INVERSE_ORIENTATIONS = SparseIntArray()
    
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