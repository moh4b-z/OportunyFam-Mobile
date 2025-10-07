package com.example.oportunyfam.model


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