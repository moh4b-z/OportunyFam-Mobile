package com.example.oportunyfam.model

data class EnderecoResponse(
    val id: Int,
    val osm_id: Long? = null,
    val nome: String? = null,
    val tipo: String? = null,
    val cep: String,
    val logradouro: String,
    val numero: String? = null,
    val complemento: String? = null,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val telefone: String? = null,
    val site: String? = null,
    val latitude: Double,
    val longitude: Double,
    val criado_em: String,
    val atualizado_em: String
)