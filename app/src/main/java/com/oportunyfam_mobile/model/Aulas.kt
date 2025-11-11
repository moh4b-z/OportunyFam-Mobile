package com.oportunyfam_mobile.model

// ============ MODELOS PARA AULAS ============

// Request para criar uma aula individual
data class AulaRequest(
    val id_atividade: Int,
    val data_aula: String, // Formato: "YYYY-MM-DD"
    val hora_inicio: String, // Formato: "HH:MM:SS"
    val hora_fim: String, // Formato: "HH:MM:SS"
    val vagas_total: Int,
    val vagas_disponiveis: Int? = null,
    val ativo: Boolean = true
)

// Response para aula criada
data class AulaCriadaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aula: AulaSimples?
)

// Aula simples (após criação)
data class AulaSimples(
    val id: Int,
    val id_atividade: Int,
    val data_aula: String,
    val hora_inicio: String,
    val hora_fim: String,
    val vagas_total: Int,
    val vagas_disponiveis: Int
)

// Request para criar várias aulas de uma vez (lote)
data class AulaLoteRequest(
    val id_atividade: Int,
    val hora_inicio: String, // Formato: "HH:MM:SS"
    val hora_fim: String, // Formato: "HH:MM:SS"
    val vagas_total: Int,
    val datas: List<String> // Lista de datas no formato "YYYY-MM-DD"
)

// Response para criação de aulas em lote
data class AulaLoteResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aulas_inseridas: List<AulaSimples>?,
    val total_inseridas: Int?,
    val erros: Any?
)

// Response para todas as aulas
data class AulasListResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aulas: List<AulaDetalhada>?
)

// Response para aula única
data class AulaUnicaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aula: AulaDetalhada?
)

// Aula com todos os detalhes (incluindo participantes)
data class AulaDetalhada(
    val aula_id: Int,
    val id_atividade: Int,
    val data_aula: String,
    val hora_inicio: String,
    val hora_fim: String,
    val vagas_total: Int,
    val vagas_disponiveis: Int,
    val status_aula: String?, // "Futura", "Hoje", "Encerrada"
    val iram_participar: List<Participante>?,
    val foram: List<Participante>?,
    val ausentes: List<Participante>?,
    val nome_atividade: String? = null, // Presente ao buscar aulas por instituição
    val instituicao_nome: String? = null // Presente ao buscar aulas por instituição
)

// Participante de uma aula (já definido em Atividade.kt mas replicado aqui para referência)
// data class Participante definido em Atividade.kt

/**
 * Modelo de Aluno - Representa uma criança inscrita em uma atividade da instituição
 */
data class Aluno(
    val instituicao_id: Int,
    val instituicao_nome: String,
    val atividade_id: Int,
    val atividade_titulo: String,
    val crianca_id: Int,
    val crianca_nome: String,
    val crianca_foto: String?,
    val status_id: Int,
    val status_inscricao: String,
    val data_inscricao: String
)

/**
 * Resposta da API para buscar alunos
 */
data class AlunosResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val alunos: List<Aluno>?
)