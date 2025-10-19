package com.oportunyfam_mobile.model

data class OSMResultado(
    val id: Long, // ID do OSM
    val nome: String,
    val tipo: String, // Ex: school, library
    val coordenadas: Coordenadas,
    val endereco: EnderecoOSM,
    val telefone: String? = null,
    val email: String? = null
)

data class Coordenadas(
    val lat: Double,
    val lon: Double
)

data class EnderecoOSM(
    val rua: String? = null,
    val numero: String? = null,
    val bairro: String? = null,
    val cidade: String? = null,
    val estado: String? = null,
    val cep: String? = null
)