package com.app.dementiaguard.Api

import com.app.dementiaguard.Model.PreferenceRequest
import com.app.dementiaguard.Model.PreferenceResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PreferenceApiService {
    @POST("user_services/create_preference")
    fun createPreference(@Body preference: PreferenceRequest): Call<PreferenceResponse>
}
