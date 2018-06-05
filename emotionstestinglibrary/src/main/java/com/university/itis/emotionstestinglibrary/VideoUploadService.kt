package com.university.itis.emotionstestinglibrary

import android.app.IntentService
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class VideoUploadService : IntentService("VideoUploadService") {
    
    private val CHANNEL_ID = "1"
    private val SERVICE_ID = 13
    
    override fun onHandleIntent(intent: Intent?) {
        var filePath = intent?.getStringExtra(ETL_VIDEO_PATH)
        val stop = intent?.getLongExtra(ETL_END_TIME, -1)
        if (stop != -1L) {
            val newFilePath = filePath?.plus("_${stop}.mp4")
            File(filePath).renameTo(File(newFilePath))
            filePath = newFilePath
        }
        uploadVideo(filePath)
    }
    
    
    private fun uploadVideo(videoPath: String?) {
        val videoFile = File(videoPath)
        val videoTime = getVideoTime(videoPath)
        createNotification(videoFile.name)
        
        val requestFile = RequestBody.create(
                MediaType.parse("multipart/form-data"),
                videoFile)
        val body = MultipartBody.Part.createFormData("file", videoFile.name, requestFile)
        
        
        apiMethods().postVideo(file = body,
                apiKey = ETL.apiKey,
                test = ETL.testId,
                testAB = ETL.testAB,
                name = videoFile.name,
                os = "a",
                start = videoTime.startTime,
                finish = videoTime.finishTime,
                numget = ETL.number++)
                .subscribe({
                    if (ETL.deleteVideos) {
                        deleteVideoFile(videoPath)
                    }
                    final("Video was uploaded))")
                }, {
                    sendVideoToServer(videoPath, this)
                    final("Video was not uploaded((")
                })
    }
    
    private fun final(message: String) {
        NotificationManagerCompat.from(this).cancelAll()
        Log.i(VideoUploadService::class.java.name, message)
    }
    
    
    private fun createNotification(videoPath: String?) {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        
        with(notificationBuilder) {
            setSmallIcon(R.drawable.ic_cloud_upload_black_24dp)
            setContentTitle(getString(R.string.content_title))
            setContentText("Updating ${videoPath}")
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }
        NotificationManagerCompat.from(this)
                .notify(1, notificationBuilder.build())
    }
    
    
}