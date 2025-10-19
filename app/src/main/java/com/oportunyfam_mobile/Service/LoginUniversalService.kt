package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile.model.LoginRequest
import com.oportunyfam_mobile.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginUniversalService {
    @POST("login")
    suspend fun loginUniversal(@Body request: LoginRequest): Response<LoginResponse>

}