package com.oportunyfam_mobile.model

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
    val data_nascimento: LocalDate,
    val cpf: String,
    val criado_em: String,
    val telefone: String?,
    val atualizado_em: String?,
    val sexo: String?,
    val nivel_usuario: String?
)


data class UsuarioRequest(
    val nome: String,
    val foto_perfil: String?,
    val email: String,
    val senha: String,
    val data_nascimento: String,
    val telefone: String?,
    val cpf: String,
    val id_sexo: Int,
    val id_tipo_nivel: Int,
    val cep: String,
    val logradouro: String,
    val numero: String?,
    val complemento: String?,
    val bairro: String,
    val cidade: String,
    val estado: String,
)