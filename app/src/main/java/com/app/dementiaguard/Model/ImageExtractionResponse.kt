package com.app.dementiaguard.Model

data class ImageExtractionResponse(
    val context_when: String,
    val context_where: String,
    val context_who: String,
    val description: String,
    val event_title: String,
    val objects: List<String> = listOf()
)
