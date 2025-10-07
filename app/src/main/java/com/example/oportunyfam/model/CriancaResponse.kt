package com.example.oportunyfam.model

import java.time.LocalDate

data class CriancaResponse(
    val id: Int,
    val nome: String,
    val foto_perfil: String? = null, // Embora não esteja na view, é útil ter
    val email: String? = null,
    val data_nascimento: LocalDate,

    // CAMPOS ADICIONAIS DA VIEW
    val idade: Int? = null, // TIMESTAMPDIFF(YEAR, ...) AS idade

    // CAMPO QUE USA NOME (VINDO DA VIEW)
    val sexo: String // s.nome AS sexo
)