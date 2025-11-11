package com.oportunyfam_mobile.model

/**
 * InstituicaoResponse - Resposta padrão da API para operações com instituição
 */
data class InstituicaoResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val instituicao: Instituicao?
)

/**
 * Instituicao - Modelo de dados da instituição
 *
 * Atualizado para corresponder à estrutura real da API
 */
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
    val tipos_instituicao: List<TipoInstituicao> = emptyList(),
    val publicacoes: List<Any> = emptyList(),
    val conversas: List<ConversaInstituicao> = emptyList(),
    val atividades: List<Any> = emptyList(),
    val logo: String? = null,
    val telefone: String? = null
)

/**
 * Endereco - Modelo de endereço da instituição
 */
data class Endereco(
    val id: Int,
    val cep: String,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val numero: String?,
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val logradouro: String,
    val complemento: String?
)

/**
 * InstituicaoRequest - Dados para criação de instituição
 */
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

/**
 * InstituicaoAtualizarRequest - Dados para atualização de instituição
 */
data class InstituicaoAtualizarRequest(
    val nome: String? = null,
    val cnpj: String? = null,
    val email: String? = null,
    val descricao: String? = null,
    val telefone: String? = null,
    val foto_perfil: String? = null  // CORRIGIDO: backend espera foto_perfil, não logo
)
