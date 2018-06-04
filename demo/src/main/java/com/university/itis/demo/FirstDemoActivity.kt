package com.university.itis.demo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.university.itis.emotionstestinglibrary.VideoRecorder

class FirstDemoActivity : AppCompatActivity() {

    private var videoRecorder: VideoRecorder? = null

    fun onClick(view: View) {
        val intent = Intent(this, SecondDemoActivity::class.java)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_first)
        videoRecorder = VideoRecorder(this)
    }

    override fun onResume() {
        videoRecorder?.startRecording()
        super.onResume()
    }

    override fun onPause() {
        videoRecorder?.stopRecording()
        super.onPause()
    }
}
