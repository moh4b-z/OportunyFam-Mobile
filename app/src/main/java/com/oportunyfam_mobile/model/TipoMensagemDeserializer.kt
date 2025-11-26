package com.oportunyfam_mobile.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Deserializador customizado para TipoMensagem
 * Converte string do JSON para o enum correto
 */
class TipoMensagemDeserializer : JsonDeserializer<TipoMensagem> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): TipoMensagem {
        val tipoString = json?.asString?.uppercase() ?: "TEXTO"
        return try {
            TipoMensagem.valueOf(tipoString)
        } catch (e: IllegalArgumentException) {
            // Se não reconhecer o tipo, retorna TEXTO como padrão
            TipoMensagem.TEXTO
        }
    }
}

