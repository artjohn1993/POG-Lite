package com.generator.poglite.api
import android.content.Context
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class GoogleSheetApi {
    companion object{
        fun create(context: Context) : GoogleSheetServices {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .client(CustomHttp.createHttp(context))
                .baseUrl("https://your.api.url/")
                .build()

            return retrofit.create(GoogleSheetServices::class.java)
        }
    }
}