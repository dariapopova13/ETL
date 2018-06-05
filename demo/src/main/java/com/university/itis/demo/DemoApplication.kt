package com.university.itis.demo

import android.app.Application
import com.university.itis.emotionstestinglibrary.ETL
import java.util.*


class DemoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        ETL.testId = UUID.randomUUID().toString()
        ETL.apiKey = "3d1b4bc9-1740-42dc-bb5c-3168e8e7ed17"
        ETL.application = this
        ETL.testAB = "a"
        ETL.initLib()
    }
    
   
}
