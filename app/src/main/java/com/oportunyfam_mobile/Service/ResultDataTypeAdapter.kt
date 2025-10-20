package com.oportunyfam_mobile.Service

import com.google.gson.*
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.model.ResultData
import com.oportunyfam_mobile.model.Usuario
import java.lang.reflect.Type

/**
 * TypeAdapter que ensina o Gson a desserializar a 'sealed class ResultData'.
 * Ele tenta inferir o tipo do objeto 'result' verificando a presença de campos chaves.
 * Este adaptador é necessário porque o Gson não consegue instanciar classes seladas/abstratas automaticamente.
 */
class ResultDataTypeAdapter : JsonDeserializer<ResultData> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ResultData {
        val jsonObject = json.asJsonObject

        // Tentativa de desserialização manual baseada nos campos presentes no objeto 'result':

        // 1. Verificar se é um Usuário (o usuário tem CPF, data_nascimento, etc.)
        // Se a resposta JSON contiver campos típicos de 'Usuario', desserializa como 'UsuarioResult'.
        if (jsonObject.has("cpf") || (jsonObject.has("data_nascimento") && !jsonObject.has("responsavel_id"))) {
            try {
                val usuario = context.deserialize<Usuario>(json, Usuario::class.java)
                return ResultData.UsuarioResult(usuario)
            } catch (e: Exception) {
                throw JsonParseException("Falha ao desserializar ResultData para Usuario: ${e.message}", e)
            }
        }

        // 2. Verificar se é uma Criança (a criança deve ter responsavel_id)
        else if (jsonObject.has("responsavel_id") || jsonObject.has("data_nascimento_crianca")) {
            try {
                val crianca = context.deserialize<Crianca>(json, Crianca::class.java)
                return ResultData.CriancaResult(crianca)
            } catch (e: Exception) {
                throw JsonParseException("Falha ao desserializar ResultData para Crianca: ${e.message}", e)
            }
        }

        // 3. Verificar se é uma Instituição (a instituição tem cnpj, nome_fantasia, etc.)
        else if (jsonObject.has("cnpj") || jsonObject.has("nome_fantasia")) {
            try {
                val instituicao = context.deserialize<Instituicao>(json, Instituicao::class.java)
                return ResultData.InstituicaoResult(instituicao)
            } catch (e: Exception) {
                throw JsonParseException("Falha ao desserializar ResultData para Instituicao: ${e.message}", e)
            }
        }

        // Se nenhum tipo for reconhecido, lança exceção.
        throw JsonParseException("Não foi possível determinar o tipo de ResultData a partir do JSON. JSON: $json")
    }
}