package com.oportunyfam_mobile.model

import androidx.compose.ui.graphics.Color

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
    val imagem: String? = null
)

data class FilteredOngResult(
    val ongs: List<OngMapMarker>,
    val totalFound: Int
)

