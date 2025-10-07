package com.example.oportunyfam.model

data class Instituicao(
    val id: Int? = null,
    val nome: String,
    val logo: String? = null,
    val cnpj: String,
    val email: String,
    val senha: String,
    val descricao: String? = null,

    // Se o POST da Instituicao cria o Endereço junto, precisamos dos dados:
    val cep: String,
    val logradouro: String,
    val numero: String? = null,
    val complemento: String? = null,
    val bairro: String,
    val cidade: String,
    val estado: String
    // O backend calcula latitude/longitude/geo
)