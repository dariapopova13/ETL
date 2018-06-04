package com.university.itis.emotionstestinglibrary

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * Created by Daria Popova on 02.06.18.
 */
const val BASE_URL = "http://192.168.1.6:3000/main/"

private fun create(): Retrofit {
    return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
}

interface RetrofitMethods {
    @POST("upload")
    @Multipart
    fun postVideo(@Part file: MultipartBody.Part,
                  @Query("api_key") apiKey: String,
                  @Query("test") test: String,
                  @Query("os") os: String,
                  @Query("test_ab") testAB: String,
                  @Query("name") name: String?,
                  @Query("start") start: Long?,
                  @Query("finish") finish: Long?,
                  @Query("number") numget: Int): Single<ResponseBody>
}

fun apiMethods(): RetrofitMethods {
    return create().create(RetrofitMethods::class.java)
}