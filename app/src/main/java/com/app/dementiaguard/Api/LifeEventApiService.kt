package com.app.dementiaguard.Api

import com.app.dementiaguard.Model.LifeEventRequest
import com.app.dementiaguard.Model.LifeEventResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LifeEventApiService {
    @POST("user_services/create_life_event")
    fun createLifeEvent(@Body lifeEvent: LifeEventRequest): Call<LifeEventResponse>
}
