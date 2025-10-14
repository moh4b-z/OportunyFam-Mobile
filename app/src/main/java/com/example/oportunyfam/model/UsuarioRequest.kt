package com.example.oportunyfam.model

import java.time.LocalDate

data class UsuarioRequest(
    val nome: String,
    val foto_perfil: String?,
    val email: String,
    val senha: String?,
    val data_nascimento: LocalDate,
    val cpf: String,
    val id_sexo: Int?,
    val sexo: String?,
    val id_tipo_nivel: Int?,
    val nivel_usuario: String?,
    val cep: String,
    val logradouro: String,
    val numero: String,
    val complemento: String?,
    val bairro: String,
    val cidade: String,
    val estado: String
)