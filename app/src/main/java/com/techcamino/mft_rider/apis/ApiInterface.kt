package com.techcamino.mft_rider.apis

import com.techcamino.mft_rider.models.MessageDetail
import com.techcamino.mft_rider.models.UserModel
import com.techcamino.mft_rider.models.orders.Data
import com.techcamino.mft_rider.models.orders.Order
import com.techcamino.mft_rider.models.orders.OrderDetail
import com.techcamino.mft_rider.models.orders.OrderHistory
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.io.File

interface ApiInterface {

    @POST("riderapp/api/checkRider")
    @FormUrlEncoded
    fun checkRider(@Field("mobile") mobile: String): Call<MessageDetail>

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

    @POST("riderapp/api/orderPickUp")
    @FormUrlEncoded
    fun updateOrderStatus(
        @Header("Authorization") toke: String,
        @Field("order_id") orderId: String,
        @Field("pickup_status") pickupStatus: String,
        @Field("reason") reason: String,
    ): Call<MessageDetail>

    @POST("riderapp/api/imageUpload")
    fun uploadImage(
        @Header("Authorization") toke: String,
        @Body file: RequestBody
    ): Call<MessageDetail>

    @POST("riderapp/api/orderDelivered")
    @FormUrlEncoded
    fun markDelevered( @Header("Authorization") toke: String,
    @Field("order_id") orderId:String ):Call<MessageDetail>

    @GET("/quotesss")
    fun getQuotes(): Call<UserModel>

    @GET("/quotesss")
    suspend fun getQuote(): Response<UserModel>
}