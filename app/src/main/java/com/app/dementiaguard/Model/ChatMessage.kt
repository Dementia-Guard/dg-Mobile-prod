package com.app.dementiaguard.Model

import android.graphics.Bitmap

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long? = System.currentTimeMillis(),
    val image: Bitmap? = null
)
