package com.oportunyfam_mobile.model


data class CriancaListResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val criancas: List<CriancaRaw>
)

data class CriancaRaw(
    val id: Int,
    val nome: String,
    val foto_perfil: String? = null,
    val email: String? = null,
    val cpf: String? = null,
    val senha: String? = null,
    val data_nascimento: String? = null,
    val criado_em: String? = null,
    val id_sexo: Int? = null
)


data class CriancaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val crianca: Crianca?
)

data class Crianca(
    val crianca_id: Int,
    val pessoa_id: Int,
    val nome: String,
    val email: String?,
    val foto_perfil: String?,
    val data_nascimento: String,
    val idade: Int,
    val criado_em: String,
    val atualizado_em: String?,
    val sexo: String?,
    val atividades_matriculadas: List<Any> = emptyList(),
    val conversas: List<ConversaInstituicao> = emptyList(),
    val responsaveis: List<UsuarioMini> = emptyList()
)

data class CriancaRequest(
    val nome: String,
    val foto_perfil: String? = null,
    val email: String? = null,
    val senha: String?,
    val cpf: String,
    val data_nascimento: String,
    val id_sexo: Int,
    val id_usuario: Int
)

data class CriancaMini(
    val id_crianca: Int,
    val id_responsavel: Int,
    val nome: String,
    val foto_perfil: String? = null,
    val id_pessoa: Int
)