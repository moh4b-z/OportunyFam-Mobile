package com.example.oportunyfam.model

import java.time.LocalDate

data class UsuarioResponse(
    val id: Int,
    val nome: String,
    val foto_perfil: String? = null,
    val email: String,
    val data_nascimento: LocalDate,
    val cpf: String,
    val criado_em: String, // TIMESTAMP vem como String

    // CAMPOS QUE USAM NOME/NÍVEL (VINDO DA VIEW)
    val sexo: String,      // s.nome AS sexo
    val tipo_nivel: String // tn.nivel AS tipo_nivel

    // A senha nunca deve ser retornada aqui.
)