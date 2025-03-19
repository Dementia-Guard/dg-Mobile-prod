package com.app.dementiaguard.Model

import com.google.gson.annotations.SerializedName

data class EvaluationRequest(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("difficulty_level") val difficultyLevel: Int,
    @SerializedName("total_time") val totalTime: Long,
    val questions: List<QuestionRequest>
)