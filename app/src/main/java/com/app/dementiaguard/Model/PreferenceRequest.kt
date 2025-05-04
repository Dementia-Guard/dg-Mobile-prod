package com.app.dementiaguard.Model

data class PreferenceRequest(
    val user_id: Int = 1,
    val hobby: String,
    val favorite_color: String,
    val favorite_food: String,
    val favorite_song: String,
    val favorite_movie: String
)
