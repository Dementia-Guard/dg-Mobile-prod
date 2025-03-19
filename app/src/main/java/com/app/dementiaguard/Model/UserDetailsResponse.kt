package com.app.dementiaguard.Model

import com.google.gson.annotations.SerializedName

data class UserDetailsResponse(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("difficulty_level")
    val difficultyLevel: Int,

    @SerializedName("recent_avg_res_time")
    val recentAvgResTime: Float,

    @SerializedName("recent_avg_score")
    val recentAvgScore: Float
)