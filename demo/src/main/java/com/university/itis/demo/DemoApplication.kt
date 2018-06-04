package com.university.itis.demo

import android.app.Application
import com.university.itis.emotionstestinglibrary.ETL
import java.util.*


class DemoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        ETL.testId = UUID.randomUUID().toString()
        ETL.apiKey = "c8b3352e-08f8-4614-bc12-00590bfd72ca"
    }
}
