package com.app.dementiaguard.Api

import com.app.dementiaguard.Model.CreateImageRequest
import com.app.dementiaguard.Model.CreateImageResponse
import com.app.dementiaguard.Model.ImageExtractionRequest
import com.app.dementiaguard.Model.ImageExtractionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ImageExtractionApiService {
    @POST("extract_services/extract")
    fun extractImageData(@Body request: ImageExtractionRequest): Call<ImageExtractionResponse>
    
    @POST("user_services/create_image")
    fun createImage(@Body request: CreateImageRequest): Call<CreateImageResponse>
}
