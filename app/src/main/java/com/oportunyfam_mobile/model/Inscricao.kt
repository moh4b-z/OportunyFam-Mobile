package com.oportunyfam_mobile_ong.model

// ============ MODELOS PARA INSCRIÇÕES ============

// Request para criar inscrição
data class InscricaoRequest(
    val id_crianca: Int,
    val id_atividade: Int,
    val id_responsavel: Int? = null // Opcional: se não enviado, é "Sugerida pela Criança"
)

// Request para atualizar inscrição (mudança de status, observação, etc)
data class InscricaoUpdateRequest(
    val id_status: Int? = null,
    val observacao: String? = null
)

// Inscrição simples (após criação)
data class InscricaoSimples(
    val id: Int,
    val id_crianca: Int,
    val id_atividade: Int,
    val id_responsavel: Int?,
    val id_status: Int,
    val observacao: String?,
    val criado_em: String,
    val atualizado_em: String?
)

// Response para inscrição criada/atualizada
data class InscricaoCriadaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val inscricao: InscricaoSimples?
)

// Inscrição detalhada (com informações completas)
data class InscricaoDetalhada(
    val inscricao_id: Int,
    val instituicao_id: Int,
    val instituicao_nome: String,
    val atividade_id: Int,
    val atividade_titulo: String,
    val crianca_id: Int,
    val crianca_nome: String,
    val crianca_foto: String?,
    val status_id: Int,
    val status_inscricao: String,
    val data_inscricao: String,
    val observacao: String?,
    val id_responsavel: Int?,
    val matriculas: String? = null // JSON string quando presente
)

// Response para lista de inscrições
data class InscricoesResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val inscricoes: List<InscricaoDetalhada>?
)

// Response para inscrição única
data class InscricaoUnicaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val inscricao: InscricaoDetalhada?
)

