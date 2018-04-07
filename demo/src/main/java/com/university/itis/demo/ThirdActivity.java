package com.university.itis.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.university.itis.emotionstestinglibrary.VideoRecorder;

/**
 * Created by Daria Popova on 07.04.18.
 */
public class ThirdActivity extends AppCompatActivity {

    private VideoRecorder videoRecorder;
    private Toast toast;

    public void onClick(View view) {
        if (view instanceof Button) {
            String text = ((Button) view).getText().toString();
            makeToast(text);
        }
    }

    private void makeToast(String text) {
        cancelToast();
        toast = Toast.makeText(this,
                "WOW)) You just pressed " + text, Toast.LENGTH_LONG);
        toast.show();
    }

    private void cancelToast() {
        if (toast != null && toast.getView().isShown()) {
            toast.cancel();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_third);
        videoRecorder = new VideoRecorder(this);
    }

    @Override
    protected void onResume() {
        videoRecorder.startRecording();
        super.onResume();
    }

    @Override
    protected void onPause() {
        cancelToast();
        videoRecorder.stopRecording();
        super.onPause();
    }
}
