package com.oportunyfam_mobile.model

enum class StatusInscricao(val id: Int, val nome: String) {
    SUGERIDA_PELA_CRIANCA(1, "Sugerida Pela Criança"),
    CANCELADA(2, "Cancelada"),
    PENDENTE(3, "Pendente"),
    APROVADA(4, "Aprovada"),
    NEGADA(5, "Negada");

    companion object {
        fun fromId(id: Int): StatusInscricao? = values().find { it.id == id }
    }
}