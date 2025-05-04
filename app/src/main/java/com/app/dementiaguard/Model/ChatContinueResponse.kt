package com.app.dementiaguard.Model

data class ChatContinueResponse(
    val message: String,
    val quiz: Boolean,
    val user_message: String
)
