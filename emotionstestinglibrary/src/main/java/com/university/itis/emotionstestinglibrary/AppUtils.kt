package com.university.itis.emotionstestinglibrary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val ETL_VIDEO_PATH = "video path"
val ETL_PERMISSION_CODE = 1
val ETL_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

fun hasAppPermissions(context: Context): Boolean {
    for (permission in ETL_PERMISSIONS) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
            return false
    }
    return true
}

fun requestAppPermissions(activity: Activity) {
    if (!hasAppPermissions(activity)) {
        ActivityCompat.requestPermissions(activity,
                ETL_PERMISSIONS,
                ETL_PERMISSION_CODE)
    }
}

val formatDate: String
    get() {
        val format = SimpleDateFormat("yyyy_MM_dd__HH_mm_ss__", Locale.getDefault())
        return format.format(Date())
    }


fun createCurrentTestDirectory(context: Context, dirName: String): Boolean {
    val dir = File(dirName)
    return dir.exists() || dir.mkdirs()
}


fun getVideoFilePath(activity: Activity): String {
    val dirName = Environment.getExternalStorageDirectory().toString() + "/" + "ETL/" + ETL.testId
    createCurrentTestDirectory(activity, dirName)
    val activityName = activity::class.java.simpleName
    val videoName = "${activityName}_${currentTime}"
    return "$dirName/$videoName"
}

val currentTime: Long
    get() {
        return System.currentTimeMillis() / 1000L
    }

val ETL_END_TIME = "etl_end_time"

fun sendVideoToServer(filePath: String?, context: Context, stop: Long? = null) {
    val videoUploadIntent = Intent(context, VideoUploadService::class.java)
    videoUploadIntent.putExtra(ETL_VIDEO_PATH, filePath).putExtra(ETL_END_TIME, stop)
    context.startService(videoUploadIntent)
}


data class VideoTime(val startTime: Long?, val finishTime: Long?)

fun getVideoTime(filePath: String?): VideoTime {
    val startTime = filePath?.substring(filePath.indexOf('_') + 1, filePath.lastIndexOf('_'))?.toLong()
    val stopTime = filePath?.substring(filePath.lastIndexOf('_') + 1, filePath.lastIndexOf('.'))?.toLong()
    return VideoTime(startTime = startTime, finishTime = stopTime)
}

fun deleteVideoFile(videoPath: String?) {
    File(videoPath).delete()
}

