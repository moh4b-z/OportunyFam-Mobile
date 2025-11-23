package com.oportunyfam_mobile.model

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

data class EnderecoRequest(
    val id: Int? = null, // Necessário no PUT
    val osm_id: Long? = null, // Pode vir da consulta OSM
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
    // Coordenadas são obrigatórias na tabela SQL
    val latitude: Double,
    val longitude: Double
)

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

data class EnderecoUser(
    val cep: String,
    val logradouro: String,
    val numero: String,
    val complemento: String,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val id_endereco: Int,
    val id_usuario_endereco: Int,
    val latitude: Double,
    val longitude: Double
)