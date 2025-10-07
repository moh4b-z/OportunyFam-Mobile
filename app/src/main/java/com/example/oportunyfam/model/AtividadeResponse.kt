package com.example.oportunyfam.model


data class AtividadeResponse(
    val atividade_id: Int, // Mapeia para 'atividade_id' da View
    val titulo: String,
    val descricao: String? = null,
    val faixa_etaria_min: Int,
    val faixa_etaria_max: Int,
    val gratuita: Boolean,
    val preco: Double,
    val ativo: Boolean,

    // CAMPOS JOIN DA VIEW
    val categoria: String, // c.nome AS categoria
    val instituicao_id: Int,
    val instituicao: String, // i.nome AS instituicao
    val cidade: String,
    val estado: String,

    // ARRAY DE HORÁRIOS DA VIEW
    val horarios: List<HorarioDetalhe> // Lista da classe definida acima (AulaResponse simplificada)
)