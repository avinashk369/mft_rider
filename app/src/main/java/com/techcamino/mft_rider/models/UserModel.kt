package com.techcamino.mft_rider.models

data class UserModel(
    val message: String,
    val status: Boolean,
    val result: Result
){
    data class Result(
        val name: String,
        val mobile: String,
        val token: String,
        val error: ErrorModel
    ){
        data class ErrorModel(var otp:String)
    }
}
