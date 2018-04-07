package com.university.itis.demo;

import android.app.Application;

import com.university.itis.emotionstestinglibrary.ETL;

/**
 * Created by Daria Popova on 06.04.18.
 */
public class DemoApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        ETL.Builder()
                .setTestId(2)
                .setApplication(this)
                .build();
    }
}
