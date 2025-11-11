package com.oportunyfam_mobile.model

// ============ MODELOS PARA CATEGORIAS ============

// Categoria simples
data class Categoria(
    val id: Int,
    val nome: String
)

// Request para criar/atualizar categoria
data class CategoriaRequest(
    val nome: String
)

// Response para categoria criada/atualizada
data class CategoriaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val categoria: Categoria?
)

// Response para lista de categorias
data class CategoriasResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val categorias: List<Categoria>?
)

