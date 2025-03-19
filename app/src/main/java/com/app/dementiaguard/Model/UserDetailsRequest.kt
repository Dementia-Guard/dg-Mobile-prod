package com.app.dementiaguard.Model

import com.google.gson.annotations.SerializedName

data class UserDetailsRequest(
    @SerializedName("user_id")
    val userId: Int
)