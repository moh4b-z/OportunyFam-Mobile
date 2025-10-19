package com.oportunyfam_mobile.model

import java.time.LocalTime

data class HorarioDetalhe(
    val id: Int,
    val dia_semana: Int,
    val inicio: LocalTime, // Mapeia para 'inicio' no JSON_OBJECT da View
    val fim: LocalTime,    // Mapeia para 'fim' no JSON_OBJECT da View
    val vagas_total: Int,
    val vagas_disponiveis: Int
)
// Esta classe é a estrutura do array JSON retornado pelo backend
data class AulaResponse(
    val id: Int,
    val id_atividade: Int,
    val dia_semana: Int,
    val hora_inicio: LocalTime,
    val hora_fim: LocalTime,
    val vagas_total: Int,
    val vagas_disponiveis: Int,
    val ativo: Boolean
)