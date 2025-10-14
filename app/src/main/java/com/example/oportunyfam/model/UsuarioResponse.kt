package com.example.oportunyfam.model

import java.time.LocalDate

data class UsuarioResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val usuario: Usuario?
)

data class Usuario(
    val id: Int,
    val nome: String,
    val foto_perfil: String?,
    val email: String,
    val senha: String?,
    val data_nascimento: LocalDate,
    val cpf: String,
    val criado_em: String,
    val atualizado_em: String?,
    val id_sexo: Int?,
    val sexo: String?,
    val id_tipo_nivel: Int?,
    val nivel_usuario: String?
)
