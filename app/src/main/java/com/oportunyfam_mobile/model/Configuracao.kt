package com.oportunyfam_mobile.model

/**
 * Modelo de dados para Configuração
 */
sealed class Configuracao {
    abstract val titulo: String
    abstract val descricao: String

    data class Toggle(
        override val titulo: String,
        override val descricao: String,
        val valor: Boolean
    ) : Configuracao()

    data class Info(
        override val titulo: String,
        override val descricao: String,
        val valor: String
    ) : Configuracao()
}