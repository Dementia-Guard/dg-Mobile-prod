package com.app.dementiaguard.Model

import com.google.gson.annotations.SerializedName

data class QuestionRequest(
    val question: String,
    val category: String,
    @SerializedName("user_answer") val userAnswer: String?,
    @SerializedName("sub_question") val subQuestion: String? = null,
    @SerializedName("correct_answer") val correctAnswer: Any? = null, // Can be String or List<String/Int>
    val options: List<String>? = null,
    val article: String? = null,
    val title: String? = null
)