package com.app.dementiaguard.Service

import com.app.dementiaguard.Model.EvaluationRequest
import com.app.dementiaguard.Model.EvaluationResponse
import com.app.dementiaguard.Model.SessionResponse
import com.app.dementiaguard.Model.UserDetailsRequest
import com.app.dementiaguard.Model.UserDetailsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("chia-service/create-question-session")
    suspend fun createQuestionSession(@Body request: Map<String, Int>): Response<SessionResponse>

    @POST("chia-service/evaluate-session")
    suspend fun evaluateSession(@Body request: EvaluationRequest): Response<EvaluationResponse>

    @POST("chia-service/user-details")
    suspend fun getUserDetails(@Body request: UserDetailsRequest): Response<UserDetailsResponse>
}