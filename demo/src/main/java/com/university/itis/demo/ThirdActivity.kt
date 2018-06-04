package com.university.itis.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.Toast

import com.university.itis.emotionstestinglibrary.VideoRecorder

/**
 * Created by Daria Popova on 07.04.18.
 */
class ThirdActivity : AppCompatActivity() {

    private var videoRecorder: VideoRecorder? = null
    private var toast: Toast? = null

    fun onClick(view: View) {
        if (view is Button) {
            val text = view.text.toString()
            makeToast(text)
        }
    }

    private fun makeToast(text: String) {
        cancelToast()
        toast = Toast.makeText(this,
                "WOW)) You just pressed $text", Toast.LENGTH_LONG)
        toast!!.show()
    }

    private fun cancelToast() {
        if (toast != null && toast!!.view.isShown) {
            toast!!.cancel()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_third)
        videoRecorder = VideoRecorder(this)
    }

    override fun onResume() {
        videoRecorder!!.startRecording()
        super.onResume()
    }

    override fun onPause() {
        cancelToast()
        videoRecorder!!.stopRecording()
        super.onPause()
    }
}
