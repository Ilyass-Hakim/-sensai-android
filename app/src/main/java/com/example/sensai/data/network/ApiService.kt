package com.example.sensai.data.network

import com.example.sensai.data.model.AuthResponse
import com.example.sensai.data.model.LoginRequest
import com.example.sensai.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

import com.example.sensai.data.network.dto.UpdateProfileDto
import com.example.sensai.data.network.dto.UserProfileDto
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("users/profile")
    suspend fun getProfile(): UserProfileDto

    @PUT("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileDto): UserProfileDto

    @Multipart
    @POST("users/avatar")
    suspend fun uploadAvatar(@Part image: MultipartBody.Part): UserProfileDto

    @GET("users/{userId}")
    suspend fun getUserById(@retrofit2.http.Path("userId") userId: Long): UserProfileDto

    @PUT("users/my-location")
    suspend fun updateLocation(@Body request: com.example.sensai.data.network.dto.LocationRequest): UserProfileDto
}
