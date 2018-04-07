package com.university.itis.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.university.itis.emotionstestinglibrary.VideoRecorder;

/**
 * Created by Daria Popova on 06.04.18.
 */
public class SecondDemoActivity extends AppCompatActivity {

    private VideoRecorder videoRecorder;

    public void onClick(View view) {
        Intent intent = new Intent(this, ThirdActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_second);
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
        super.onPause();
    }
}
