package com.oportunyfam_mobile.model

data class LoginResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val tipo: String,
    val instituicao: Instituicao?,
    val crianca: Crianca?,
    val usuario: Usuario?
)