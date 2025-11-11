package com.oportunyfam_mobile.model

// ============ MODELOS PARA MATRÍCULAS (PRESENÇA EM AULAS) ============

// Request para criar matrícula (registrar presença/ausência)
data class MatriculaRequest(
    val id_inscricao_atividade: Int,
    val id_aula_atividade: Int,
    val presente: Boolean,
    val nota_observacao: String? = null
)

// Request para atualizar matrícula
data class MatriculaUpdateRequest(
    val presente: Boolean? = null,
    val nota_observacao: String? = null
)

// Matrícula simples
data class Matricula(
    val id: Int,
    val id_inscricao_atividade: Int,
    val id_aula_atividade: Int,
    val presente: Boolean,
    val nota_observacao: String?,
    val criado_em: String,
    val atualizado_em: String?
)

// Response para matrícula criada/atualizada
data class MatriculaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val matricula: Matricula?
)

// Response para lista de matrículas
data class MatriculasResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val matriculas: List<Matricula>?
)

