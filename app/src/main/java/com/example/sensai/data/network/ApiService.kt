package com.example.sensai.data.network

import com.example.sensai.data.model.AuthResponse
import com.example.sensai.data.model.LoginRequest
import com.example.sensai.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("users/profile")
    suspend fun getProfile(): AuthResponse // Assuming profile returns similar info for now
}
