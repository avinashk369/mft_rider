package com.techcamino.mft_rider.models

data class UserModel(
    val count: Int,
    val lastItemIndex: Int,
    val page: Int,
    val results: List<Result>,
    val totalCount: Int,
    val totalPages: Int,
    val status: Boolean
)
