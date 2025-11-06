
package com.oportunyfam_mobile.model


data class InstituicaoResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val instituicao: Instituicao?
)

data class Instituicao(
    val instituicao_id: Int,
    val pessoa_id: Int,
    val nome: String,
    val email: String,
    val foto_perfil: String?,
    val cnpj: String,
    val descricao: String?,
    val criado_em: String,
    val atualizado_em: String?,
    val endereco: Endereco?,
    val tipos_instituicao: List<Any> = emptyList(),
    val publicacoes: List<Any> = emptyList(),
    val conversas: List<Any> = emptyList(),
    val atividades: List<Any> = emptyList(),
    val logo: String?,
    val telefone: String?
)

data class Endereco(
    val id: Int,
    val cep: String,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val numero: String?,
    val latitude: Double?,
    val longitude: Double?,
    val logradouro: String,
    val complemento: String?
)

data class InstituicaoRequest(
    val nome: String,
    val logo: String? = null,
    val email: String,
    val senha: String,
    val cnpj: String,
    val descricao: String? = null,
    val cep: String,
    val logradouro: String,
    val numero: String?,
    val complemento: String?,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val telefone: String?,
    val tipos_instituicao: List<Int>
)



data class InstituicaoAtualizarRequest(
    val nome: String? = null,
    val cnpj: String? = null,
    val email: String? = null,
    val descricao: String? = null,
    val telefone: String? = null,
    val logo: String? = null
)
