package com.example.oportunyfam.model

import java.time.LocalTime

data class AulaRequest(
    val id: Int? = null, // Para PUT
    val id_atividade: Int, // Chave estrangeira obrigatória
    val dia_semana: Int, // 1=Dom, 2=Seg, ...
    val hora_inicio: LocalTime,
    val hora_fim: LocalTime,
    val vagas_total: Int,
    val vagas_disponiveis: Int, // Opcional, pode ser calculado no backend
    val ativo: Boolean = true
)