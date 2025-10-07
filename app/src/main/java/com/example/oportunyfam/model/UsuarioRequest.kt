package com.example.oportunyfam.model

import java.time.LocalDate

data class UsuarioRequest(
    val id: Int? = null, // Necessário no PUT
    val nome: String,
    val foto_perfil: String? = null,
    val email: String,
    val senha: String,
    val data_nascimento: LocalDate,
    val cpf: String,

    // CAMPOS QUE USAM ID (OBRIGATÓRIOS NO ENVIO)
    val id_sexo: Int,
    val id_tipo_nivel: Int
)