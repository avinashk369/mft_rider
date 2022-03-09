package com.techcamino.mft_rider.apis

import com.techcamino.mft_rider.models.UserModel
import com.techcamino.mft_rider.models.orders.Data
import com.techcamino.mft_rider.models.orders.Order
import com.techcamino.mft_rider.models.orders.OrderDetail
import com.techcamino.mft_rider.models.orders.OrderHistory
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    @POST("riderapp/api/loginRider")
    @FormUrlEncoded
    fun checkLogin(@Field("mobile") mobile: String, @Field("otp") otp: String): Call<UserModel>

    @POST("riderapp/api/orders_history")
    fun getOrderHistory(@Header("Authorization") toke: String): Call<Data>

    @POST("riderapp/api/orders")
    @FormUrlEncoded
    fun getAllOrders(
        @Header("Authorization") toke: String,
        @Field("type") type: String,
        @Field("page") page: String,
    ): Call<Order>

    @POST("riderapp/api/orderDetail")
    @FormUrlEncoded
    fun getOrderDetail(
        @Header("Authorization") toke: String,
        @Field("order_id") orderId: String,
    ): Call<OrderDetail>

    @GET("/quotesss")
    fun getQuotes(): Call<UserModel>

    @GET("/quotesss")
    suspend fun getQuote(): Response<UserModel>
}