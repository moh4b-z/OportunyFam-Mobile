package com.oportunyfam_mobile.model

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * LoginResponse - Resposta da API de login
 *
 * Estrutura atualizada para corresponder à resposta real da API:
 * - result contém os dados da instituição/usuário diretamente
 * - accessToken é retornado no nível raiz
 */
data class LoginResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val tipo: String, // "instituicao" ou "usuario"
    val result: ResultData?,
    val accessToken: String? // Token de acesso JWT
)

/**
 * ResultData - Dados do resultado do login
 *
 * Pode ser dados de uma instituição ou usuário
 */
sealed class ResultData {
    data class InstituicaoResult(val data: Instituicao) : ResultData()
    data class UsuarioResult(val data: Usuario) : ResultData()
}

/**
 * LoginResponseDeserializer - Deserializador customizado para LoginResponse
 *
 * Lida com a estrutura da API onde o tipo determina se é instituição ou usuário
 */
class LoginResponseDeserializer : JsonDeserializer<LoginResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LoginResponse {
        val jsonObject = json.asJsonObject

        val status = jsonObject.get("status").asBoolean
        val statusCode = jsonObject.get("status_code").asInt
        val messagem = jsonObject.get("messagem").asString
        val tipo = jsonObject.get("tipo").asString
        val accessToken = jsonObject.get("accessToken")?.asString

        val resultData: ResultData? = if (jsonObject.has("result") && !jsonObject.get("result").isJsonNull) {
            val resultElement = jsonObject.get("result")
            try {
                when (tipo) {
                    "instituicao" -> {
                        val instituicao = context.deserialize<Instituicao>(resultElement, Instituicao::class.java)
                        ResultData.InstituicaoResult(instituicao)
                    }
                    "usuario" -> {
                        val usuario = context.deserialize<Usuario>(resultElement, Usuario::class.java)
                        ResultData.UsuarioResult(usuario)
                    }
                    else -> null
                }
            } catch (e: Exception) {
                Log.e("LoginResponseDeserializer", "Erro ao deserializar result: ${e.message}", e)
                null
            }
        } else {
            null
        }

        return LoginResponse(
            status = status,
            status_code = statusCode,
            messagem = messagem,
            tipo = tipo,
            result = resultData,
            accessToken = accessToken
        )
    }
}

/**
 * LoginRequest - Dados para requisição de login
 */
data class LoginRequest(
    val email: String,
    val senha: String
)