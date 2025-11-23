package com.oportunyfam_mobile.model

data class OngMapMarker(
    val id: Int,
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val categorias: List<Int>, // Lista de IDs de categorias
    val descricao: String,
    val endereco: String,
    val telefone: String,
    val email: String,
    val imagem: String? = null,
    // Novo: indica se o marcador veio de uma fonte externa (Google Places). Se true, clicar deve abrir a ficha/mini-aba.
    val isExternal: Boolean = false,
    // Quando é externo, pode conter placeId do Google Places para abrir no Maps
    val placeId: String? = null
)
