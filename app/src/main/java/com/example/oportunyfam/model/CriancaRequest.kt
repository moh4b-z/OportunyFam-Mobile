package com.example.oportunyfam.model

import java.time.LocalDate

data class CriancaRequest(
    val id: Int? = null, // Necessário no PUT
    val nome: String,
    val foto_perfil: String? = null,
    val email: String? = null, // Opcional no banco
    val cpf: String,
    val senha: String,
    val data_nascimento: LocalDate,

    // CAMPO QUE USA ID (OBRIGATÓRIO NO ENVIO)
    val id_sexo: Int
)