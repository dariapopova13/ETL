package com.university.itis.etl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.university.itis.emotionstestinglibrary.CameraHelper;

/**
 * Created by Daria Popova on 06.04.18.
 */
public class SecondActivity extends AppCompatActivity {


    private CameraHelper cameraHelper;

    public void onClick(View view) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_activity);
        cameraHelper = new CameraHelper(this);
    }


    @Override
    protected void onResume() {
        cameraHelper.startRecording();
        super.onResume();
    }

    @Override
    protected void onPause() {
        cameraHelper.stopRecording();
        super.onPause();
    }
}