package com.example.oportunyfam.model

data class AtividadeRequest(
    val id: Int? = null, // Para PUT
    val id_instituicao: Int, // Chave estrangeira obrigatória
    val id_categoria: Int, // Chave estrangeira obrigatória
    val titulo: String,
    val descricao: String? = null,
    val faixa_etaria_min: Int,
    val faixa_etaria_max: Int,
    val gratuita: Boolean = true,
    val preco: Double = 0.0,
    val ativo: Boolean = true
)