package com.oportunyfam_mobile.model

data class SexosResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val sexos: List<Sexo>
)

data class Sexo(
    val id: Int,
    val nome: String
)
