package com.techcamino.mft_rider.apis

import com.techcamino.mft_rider.models.UserModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    @POST("riderapp/api/loginRider")
    @FormUrlEncoded
    fun checkLogin(@Field("mobile") mobile:String, @Field("otp") otp:String):Call<UserModel>

    @GET("/quotesss")
    fun getQuotes(): Call<UserModel>
    @GET("/quotesss")
    suspend fun getQuote(): Response<UserModel>
}