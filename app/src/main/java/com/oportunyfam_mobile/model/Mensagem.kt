package com.oportunyfam_mobile.model

data class Mensagem(
    val id: Int,
    val descricao: String,
    val visto: Boolean,
    val criado_em: String,
    val atualizado_em: String?,
    val id_conversa: Int,
    val id_pessoa: Int
)

data class MensagemResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val mensagem: Mensagem?
)

data class MensagensResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val mensagens: List<Mensagem>?
)

data class MensagemRequest(
    val id_conversa: Int,
    val id_pessoa: Int,
    val descricao: String
)

data class MensagemAtualizarRequest(
    val descricao: String? = null,
    val visto: Boolean? = null
)