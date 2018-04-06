package com.university.itis.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.university.itis.emotionstestinglibrary.VideoRecorder;

public class FirstDemoActivity extends AppCompatActivity {

    private VideoRecorder videoRecorder;

    public void onClick(View view) {
        Intent intent = new Intent(this, SecondDemoActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_first);
        videoRecorder = new VideoRecorder(this);
    }

    @Override
    protected void onResume() {
        videoRecorder.startRecording();
        super.onResume();
    }

    @Override
    protected void onPause() {
        videoRecorder.stopRecording();
        super.onStop();
    }
}
