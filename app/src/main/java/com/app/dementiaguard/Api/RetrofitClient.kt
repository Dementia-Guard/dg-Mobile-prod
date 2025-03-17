package com.app.dementiaguard.Api

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.pow

object RetrofitClient {
    private const val BASE_URL = "http://192.168.8.185:8080/"
    private const val TIMEOUT_SECONDS = 30L
    private const val MAX_RETRIES = 3

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()
    
    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }
    
    private val retryInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var response: Response? = null
            var exception: IOException? = null
            
            for (attempt in 0 until MAX_RETRIES) {
                try {
                    if (attempt > 0) {
                        val waitTime = (2.0.pow(attempt.toDouble()) * 1000).toLong()
                        Thread.sleep(waitTime.coerceAtMost(10000))
                    }
                    
                    response = chain.proceed(request)
                    
                    if (response.isSuccessful) {
                        return response
                    } else {
                        response.close()
                    }
                } catch (e: IOException) {
                    exception = e
                    if (attempt == MAX_RETRIES - 1) {
                        throw e
                    }
                }
            }
            
            return response ?: throw exception ?: IOException("Unknown error")
        }
    }
    
    private val connectionPool = ConnectionPool(5, 30, TimeUnit.SECONDS)
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(retryInterceptor)
        .cookieJar(cookieJar)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .connectionPool(connectionPool)
        .retryOnConnectionFailure(true)
        .build()
    
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}
