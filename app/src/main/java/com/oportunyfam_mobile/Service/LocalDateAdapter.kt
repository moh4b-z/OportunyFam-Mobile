package com.oportunyfam_mobile.Service

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Adaptador personalizado para deserializar Strings de data ISO 8601 (incluindo tempo e fuso,
 * ex: "2007-08-31T00:00:00.000Z") para um objeto LocalDate, ignorando o tempo.
 */
class LocalDateAdapter : JsonDeserializer<LocalDate> {

    // Define o formato de data ISO 8601 (apenas a parte da data)
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LocalDate {
        val dateString = json.asString

        try {
            // A string recebida é "YYYY-MM-DDT00:00:00.000Z".
            // Pegamos apenas a parte da data "YYYY-MM-DD" antes do 'T'.
            val dateOnly = dateString.substringBefore("T")

            // Usamos o formatter para converter "YYYY-MM-DD" para LocalDate
            return LocalDate.parse(dateOnly, dateFormatter)
        } catch (e: DateTimeParseException) {
            // Em caso de falha na análise (parsing), logamos o erro e lançamos uma exceção.
            throw JsonParseException("Não foi possível analisar a data: $dateString", e)
        }
    }
}