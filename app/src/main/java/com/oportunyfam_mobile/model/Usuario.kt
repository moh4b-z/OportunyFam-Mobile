package com.oportunyfam_mobile.model

import com.oportunyfam_mobile_ong.model.Crianca

data class UsuarioResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val usuario: Usuario?
)

data class Usuario(
    val usuario_id: Int,
    val pessoa_id: Int,
    val nome: String,
    val foto_perfil: String?,
    val email: String,
    val data_nascimento: String,
    val cpf: String,
    val criado_em: String,
    val telefone: String?,
    val atualizado_em: String?,
    val sexo: String?,
    val tipo_nivel: String?,
    val criancas_dependentes: List<Crianca> = emptyList(),
    val conversas: List<Any> = emptyList(),
    val id: Int
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
    val estado: String
)