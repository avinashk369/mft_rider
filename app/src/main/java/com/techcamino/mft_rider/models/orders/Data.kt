package com.techcamino.mft_rider.models.orders

import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("status" ) var status : Boolean? = null,
    @SerializedName("result" ) var result : OrderHistory?  =OrderHistory()
)
