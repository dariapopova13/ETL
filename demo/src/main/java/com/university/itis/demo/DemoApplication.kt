package com.university.itis.demo

import android.app.Application
import com.university.itis.emotionstestinglibrary.ETL
import java.util.*


class DemoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        ETL.testId = UUID.randomUUID().toString()
        ETL.apiKey = "76596227-e453-4497-8485-8276452884e7"
        ETL.application = this
        ETL.testAB = "a"
        ETL.initLib()
    }
    
   
}
