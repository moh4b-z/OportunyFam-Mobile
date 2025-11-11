package com.oportunyfam_mobile.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de Publicação (response da API)
 */
data class Publicacao(
    val id: Int,
    val id_instituicao: Int,
    val descricao: String?,
    val imagem: String?,
    val criado_em: String,
    val atualizado_em: String?
)

/**
 * Response para criar publicação
 */
data class PublicacaoCriadaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val publicacao_instituicao: Publicacao?
)

/**
 * Response para listar publicações
 */
data class PublicacoesListResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    @SerializedName("publicacoes_instituicao")
    val publicacoes: List<Publicacao>?
)

/**
 * Request para criar/atualizar publicação
 */
data class PublicacaoRequest(
    @SerializedName("descricao")
    val descricao: String,

    @SerializedName("imagem")
    val imagem: String?,

    @SerializedName("instituicao_id")
    val instituicaoId: Int
)

/**
 * Response de erro da API
 */
data class ErrorResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String
)

