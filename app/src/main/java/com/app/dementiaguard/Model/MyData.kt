package com.app.dementiaguard.Model

import com.app.dementiaguard.Model.Location

data class MyData(
    val temperature: String = "",
    val location: Location = Location(),
    val pulseRate: Int = 0,
    val stepCount: Int = 0,
    val bloodOxygen: String = "",
    val isSOS: Boolean = false
)
