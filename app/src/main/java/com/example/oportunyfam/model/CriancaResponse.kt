package com.example.oportunyfam.model

import java.time.LocalDate

data class CriancaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val crianca: Crianca?
)

data class Crianca(
    val id: Int,
    val nome: String,
    val foto_perfil: String?,
    val email: String,
    val cpf: String,
    val senha: String?,
    val data_nascimento: LocalDate,
    val criado_em: String,
    val id_sexo: Int?,
    val sexo: String?
)
