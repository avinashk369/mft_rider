package com.techcamino.mft_rider.models

data class MessageDetail(
    var status:Boolean,
    var message:String,
    var result: Result
    ){
    data class Result(val error:ErrorModel){
        data class ErrorModel(var otp:String,var mobile:String)
    }
}
