package com.app.dementiaguard.Model

import com.google.gson.annotations.SerializedName

data class PreviousSession(
    @SerializedName("adjusted_difficulty_level")
    val adjustedDifficultyLevel: Int,

    @SerializedName("avg_res_time")
    val avgResTime: Float,

    @SerializedName("avg_score")
    val avgScore: Float,

    @SerializedName("current_difficulty_level")
    val currentDifficultyLevel: Int,

    @SerializedName("session_id")
    val sessionId: String
)

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
    val recentAvgScore: Float,

    @SerializedName("previous_sessions")
    val previousSessions: List<PreviousSession>
)