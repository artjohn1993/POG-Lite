package com.generator.poglite.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class CustomHttp{
    companion object{
        fun createHttp(context: Context) : OkHttpClient {
            val okhttp = OkHttpClient.Builder()
            val interceptor = Interceptor { chain ->
                var request: Request? = null

                request = chain?.request()?.newBuilder()
                    ?.addHeader("Content-Type", "application/json")
                    ?.addHeader("api-version",
                        "1.0")
                    ?.build()

                chain.proceed(request)
            }
            return  okhttp.build()
        }
    }
}

class WordpressApi {
    companion object{
        fun create(context: Context) : ApiServices {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .client(CustomHttp.createHttp(context))
                .baseUrl("https://your.api.url/")
                .build()    

            return retrofit.create(ApiServices::class.java)
        }
    }
}