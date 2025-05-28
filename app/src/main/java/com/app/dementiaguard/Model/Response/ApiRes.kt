package com.app.dementiaguard.Model.Response

import com.google.gson.annotations.SerializedName

data class ApiRes<T>(
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("data")
    val data: T
)
