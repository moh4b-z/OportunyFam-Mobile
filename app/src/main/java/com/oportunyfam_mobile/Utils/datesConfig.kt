package com.oportunyfam_mobile.Utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun formatarDataNascimentoParaLocalDate(dataNascimento: String): LocalDate? {
    if (dataNascimento.length != 10) return null
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(dataNascimento, inputFormatter)

        // Impede datas futuras
        if (date.isAfter(LocalDate.now())) {
            return null
        }
        return date
    } catch (e: DateTimeParseException) {
        null
    }
}

fun convertDataParaBackendFormat(dataNascimento: String): String {
    // dataNascimento deve estar no formato "DD/MM/YYYY"
    if (dataNascimento.length != 10) return ""
    return try {
        val parts = dataNascimento.split("/")
        // parts[0] = DD, parts[1] = MM, parts[2] = YYYY
        // Backend espera: YYYY-MM-DD
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } catch (e: Exception) {
        ""
    }
}

fun aplicarMascaraDataNascimento(newValue: String): String {
    val cleanValue = newValue.filter { it.isDigit() }.take(8)
    return when {
        cleanValue.length > 4 -> "${cleanValue.substring(0, 2)}/${cleanValue.substring(2, 4)}/${cleanValue.substring(4)}"
        cleanValue.length > 2 -> "${cleanValue.substring(0, 2)}/${cleanValue.substring(2)}"
        else -> cleanValue
    }
}
