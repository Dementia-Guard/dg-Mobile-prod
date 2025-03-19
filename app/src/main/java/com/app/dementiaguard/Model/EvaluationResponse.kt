package com.app.dementiaguard.Model

import com.google.gson.annotations.SerializedName

data class EvaluationResponse(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("difficulty_level") val difficultyLevel: Int,
    val evaluations: List<QuestionEvaluation>,
    @SerializedName("avg_score") val avgScore: Double,
    @SerializedName("avg_time") val avgTime: Double
)
