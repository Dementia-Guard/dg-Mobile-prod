package com.app.dementiaguard.Model

data class SessionResponse(
    val session_id: String,
    val user_id: String,
    val difficulty_level: Int,
    val questions: List<Question>
)
