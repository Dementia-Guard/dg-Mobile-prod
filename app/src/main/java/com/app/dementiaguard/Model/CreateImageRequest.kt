package com.app.dementiaguard.Model

data class CreateImageRequest(
    val user_id: Int,
    val image_base64: String,
    val context_who: List<String>,
    val context_where: String,
    val context_when: String,
    val event_title: String,
    val description: String
)
