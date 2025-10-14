package com.example.oportunyfam.model

data class LoginResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val tipo: String,
    val result: ResultLogin
)

data class ResultLogin(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val instituicao: Instituicao?,
    val crianca: Crianca?,
    val usuario: Usuario?
)
