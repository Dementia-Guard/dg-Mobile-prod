package com.app.dementiaguard.Model

import com.google.gson.annotations.SerializedName

data class QuestionEvaluation(
    val question: String,
    val category: String,
    @SerializedName("user_answer") val userAnswer: String?,
    @SerializedName("correct_answer") val correctAnswer: Any?, // Can be String or List<String/Int>
    val score: Double,
    val correct: Boolean
)