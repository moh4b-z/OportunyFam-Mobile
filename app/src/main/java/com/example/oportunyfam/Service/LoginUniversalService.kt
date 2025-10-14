package com.example.oportunyfam.Service

import com.example.oportunyfam.model.LoginRequest
import com.example.oportunyfam.model.LoginResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginUniversalService {
    @POST("login")
    suspend fun loginUniversal(@Body request: LoginRequest): Response<LoginResponse>

}