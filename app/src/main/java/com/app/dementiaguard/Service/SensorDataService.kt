package com.app.dementiaguard.Service

import com.app.dementiaguard.Model.Dto.MonthlySensorDataDto
import com.app.dementiaguard.Model.Response.ApiRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SensorDataService {

    @GET("device-service/route/sensor/data/monthly")
    fun getMonthlySensorData(): Call<ApiRes<List<MonthlySensorDataDto>>>
}
