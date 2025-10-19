package com.oportunyfam_mobile.model

import java.time.LocalDate

data class CriancaRequest(
    val id: Int? = null,
    val nome: String,
    val foto_perfil: String? = null,
    val email: String? = null,
    val senha: String?,
    val cpf: String,
    val data_nascimento: LocalDate,
    val id_sexo: Int
)
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
    val telefone: String?,
    val cpf: String,
    val data_nascimento: LocalDate,
    val criado_em: String,
    val sexo: String?
)
