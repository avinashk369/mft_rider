package com.techcamino.mft_rider.models.orders

import com.google.gson.annotations.SerializedName

data class OrderHistory(
    @SerializedName("accepted_orders"  ) var acceptedOrders  : Int? = null,
    @SerializedName("delivered_orders" ) var deliveredOrders : Int? = null,
    @SerializedName("pending_orders"   ) var pendingOrders   : Int? = null
)
