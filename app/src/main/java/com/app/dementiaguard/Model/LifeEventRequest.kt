package com.app.dementiaguard.Model

data class LifeEventRequest(
    val user_id: Int = 1,
    val event_title: String,
    val event_date: String,
    val description: String,
    val emotions: List<String> = listOf()
)
