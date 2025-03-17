package com.app.dementiaguard.Api

import com.app.dementiaguard.Model.ChatContinueResponse
import com.app.dementiaguard.Model.ChatRequest
import com.app.dementiaguard.Model.ChatResponse
import com.app.dementiaguard.Model.QuizRequest
import com.app.dementiaguard.Model.QuizResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApiService {
    @GET("chatbot_services/start_chat/{user_id}")
    fun startChat(@Path("user_id") userId: Int): Call<ChatResponse>
    
    @POST("chatbot_services/chat/{session_id}")
    fun continueChat(
        @Path("session_id") sessionId: Int,
        @Body request: ChatRequest
    ): Call<ChatContinueResponse>
    
    @POST("chatbot_services/quiz/{session_id}")
    fun submitQuizAnswer(
        @Path("session_id") sessionId: Int,
        @Body request: QuizRequest
    ): Call<QuizResponse>
}
