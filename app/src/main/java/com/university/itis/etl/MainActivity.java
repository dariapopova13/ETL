package com.university.itis.etl;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.university.itis.emotionstestinglibrary.CameraHelper;

public class MainActivity extends AppCompatActivity {


    private CameraHelper cameraHelper;

    public void onClick(View view) {
        if (view.getId() == R.id.button){
            Intent intent = new Intent(this, SecondActivity.class);
            startActivity(intent);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraHelper = new CameraHelper(this);
    }


    @Override
    protected void onResume() {
        if (cameraHelper != null) {
            cameraHelper.startRecording();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (cameraHelper != null)
            cameraHelper.stopRecording();
        super.onPause();
    }
}
