package com.oportunyfam_mobile.Utils

import kotlin.math.*

/**
 * Calcula distância em quilômetros entre dois pontos usando a fórmula de Haversine
 */
fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // raio da Terra em km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

/**
 * Normaliza um CEP/zipcode removendo qualquer caractere não numérico
 */
fun normalizeCep(cep: String?): String? {
    if (cep == null) return null
    val digits = cep.replace("\\D".toRegex(), "")
    return if (digits.isBlank()) null else digits
}

