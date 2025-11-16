package com.oportunyfam_mobile.model

import com.google.gson.annotations.SerializedName

/**
 * InstituicaoResponse - Resposta padrão da API para operações com instituição
 */
data class InstituicaoResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val instituicao: Instituicao?
)

data class InstituicaoListResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val instituicoes: List<Instituicao>
)

/**
 * Instituicao - Modelo de dados da instituição
 *
 * Atualizado para corresponder à estrutura real da API
 */
data class Instituicao(
    @SerializedName("instituicao_id")
    val instituicao_id: Int,
    @SerializedName("pessoa_id")
    val pessoa_id: Int,
    @SerializedName("nome")
    val nome: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("foto_perfil")
    val foto_perfil: String?,
    @SerializedName("cnpj")
    val cnpj: String,
    @SerializedName("descricao")
    val descricao: String?,
    @SerializedName("criado_em")
    val criado_em: String,
    @SerializedName("atualizado_em")
    val atualizado_em: String?,
    @SerializedName("endereco")
    val endereco: Endereco?,
    @SerializedName("tipos_instituicao")
    val tipos_instituicao: List<TipoInstituicao> = emptyList(),
    @SerializedName("publicacoes")
    val publicacoes: List<Any> = emptyList(),
    @SerializedName("conversas")
    val conversas: List<ConversaInstituicao> = emptyList(),
    @SerializedName("atividades")
    val atividades: List<Any> = emptyList(),
    @SerializedName("logo")
    val logo: String? = null,
    @SerializedName("telefone")
    val telefone: String? = null
)

/**
 * Endereco - Modelo de endereço da instituição
 */
data class Endereco(
    @SerializedName("id")
    val id: Int,
    @SerializedName("cep")
    val cep: String,
    @SerializedName("bairro")
    val bairro: String,
    @SerializedName("cidade")
    val cidade: String,
    @SerializedName("estado")
    val estado: String,
    @SerializedName("numero")
    val numero: String?,
    @SerializedName("latitude")
    val latitude: Double? = 0.0,
    @SerializedName("longitude")
    val longitude: Double? = 0.0,
    @SerializedName("logradouro")
    val logradouro: String,
    @SerializedName("complemento")
    val complemento: String?
)

/**
 * InstituicaoRequest - Dados para criação de instituição
 */
data class InstituicaoRequest(
    @SerializedName("nome")
    val nome: String,
    @SerializedName("logo")
    val logo: String? = null,
    @SerializedName("email")
    val email: String,
    @SerializedName("senha")
    val senha: String,
    @SerializedName("cnpj")
    val cnpj: String,
    @SerializedName("descricao")
    val descricao: String? = null,
    @SerializedName("cep")
    val cep: String,
    @SerializedName("logradouro")
    val logradouro: String,
    @SerializedName("numero")
    val numero: String?,
    @SerializedName("complemento")
    val complemento: String?,
    @SerializedName("bairro")
    val bairro: String,
    @SerializedName("cidade")
    val cidade: String,
    @SerializedName("estado")
    val estado: String,
    @SerializedName("telefone")
    val telefone: String?,
    @SerializedName("tipos_instituicao")
    val tipos_instituicao: List<Int>
)

/**
 * InstituicaoAtualizarRequest - Dados para atualização de instituição
 */
data class InstituicaoAtualizarRequest(
    @SerializedName("nome")
    val nome: String? = null,
    @SerializedName("cnpj")
    val cnpj: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("descricao")
    val descricao: String? = null,
    @SerializedName("telefone")
    val telefone: String? = null,
    @SerializedName("foto_perfil")
    val foto_perfil: String? = null
)
