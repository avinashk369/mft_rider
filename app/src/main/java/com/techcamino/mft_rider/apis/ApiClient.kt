package com.techcamino.mft_rider.apis


import com.techcamino.mft_rider.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {
    val baseUrl = "https://partner.myflowertree.com/"//""https://quotable.io/"
    val apiInterface: ApiInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface = retrofit.create(ApiInterface::class.java)
    }

    private fun getHttpClient():OkHttpClient{
        if(BuildConfig.DEBUG){
            // disable logger in production
        }
    val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)
        return httpClient.build()
    }
}