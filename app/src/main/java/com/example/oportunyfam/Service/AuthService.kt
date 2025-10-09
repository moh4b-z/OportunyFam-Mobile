package com.example.oportunyfam.Service

import com.example.oportunyfam.model.LoginRequest
import com.example.oportunyfam.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    // POST - Login Universal (Endpoint: /v1/auth/login)
    @POST("/login")
    fun loginUniversal(@Body request: LoginRequest): Call<LoginResponse>
}