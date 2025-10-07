package com.example.oportunyfam.model

data class LoginResponse(
    val type: String, // "usuario", "instituicao", "crianca"
    val id: Int,
    val nome: String,
    val email: String
    // ... outros dados do usuário logado
)