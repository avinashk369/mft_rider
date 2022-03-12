package com.techcamino.mft_rider.models

import com.google.gson.annotations.SerializedName

data class MessageDetail(
    var status: Boolean,
    var message: String,
    var result: Result
) {
    data class Result(
        val error: ErrorModel,
        @SerializedName("vendor_image") var vendorImage: String? = null
    ) {
        data class ErrorModel(var otp: String, var mobile: String)
    }
}
