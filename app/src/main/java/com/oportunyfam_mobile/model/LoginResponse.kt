package com.oportunyfam_mobile.model

data class LoginResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val tipo: String,
    val result: ResultData
)
sealed class ResultData {
    data class InstituicaoResult(val data: Instituicao) : ResultData()
    data class CriancaResult(val data: Crianca) : ResultData()
    data class UsuarioResult(val data: Usuario) : ResultData()
}
