package com.oportunyfam_mobile.Service

import com.oportunyfam_mobile_ong.model.LoginRequest
import com.oportunyfam_mobile_ong.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginUniversalService {
    @Headers("Content-Type: application/json")
    @POST("login")
    suspend fun loginUniversal(@Body request: LoginRequest): Response<LoginResponse>

}