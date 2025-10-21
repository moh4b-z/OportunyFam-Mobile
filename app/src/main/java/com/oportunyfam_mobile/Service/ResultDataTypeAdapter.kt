package com.oportunyfam_mobile.Service

import com.google.gson.*
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.model.ResultData
import com.oportunyfam_mobile.model.Usuario
import java.lang.reflect.Type

/**
 * TypeAdapter que ensina o Gson a desserializar a 'sealed class ResultData'.
 * Ele é necessário porque o Gson não consegue instanciar classes seladas/abstratas automaticamente.
 * * Correção: Usamos um objeto Gson "limpo" (innerGson) para desserializar as classes internas
 * (Usuario, Crianca, Instituicao). Isso evita a corrupção do contexto de desserialização
 * que estava causando o erro 'Expected BEGIN_OBJECT but was STRING' no campo data_nascimento.
 */
class ResultDataTypeAdapter : JsonDeserializer<ResultData> {

    companion object {
        // Objeto Gson limpo, sem o ResultDataTypeAdapter registrado.
        // Ele garante que a desserialização de Usuario, Crianca, etc., use o mapeamento padrão e funcione.
        private val innerGson = GsonBuilder()
            .setLenient()
            .create()
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ResultData {
        val jsonObject = json.asJsonObject

        // Tentativa de desserialização manual baseada nos campos presentes no objeto 'result':

        // 1. Verificar se é um Usuário (o usuário tem CPF, data_nascimento, etc.)
        if (jsonObject.has("cpf") || (jsonObject.has("data_nascimento") && !jsonObject.has("responsavel_id"))) {
            try {
                // CORREÇÃO: Usamos o innerGson (limpo) para deserializar o objeto Usuario
                // O método .fromJson(jsonObject, ...) é mais seguro do que context.deserialize(...) aqui.
                val usuario = innerGson.fromJson(jsonObject, Usuario::class.java)
                return ResultData.UsuarioResult(usuario)
            } catch (e: Exception) {
                // Adicionamos o JSON à exceção para ajudar na depuração, caso haja outro problema.
                throw JsonParseException("Falha ao desserializar ResultData para Usuario: ${e.message}. JSON: $jsonObject", e)
            }
        }

        // 2. Verificar se é uma Criança (a criança deve ter responsavel_id)
        else if (jsonObject.has("responsavel_id") || jsonObject.has("data_nascimento_crianca")) {
            try {
                // CORREÇÃO: Usamos o innerGson para Crianca
                val crianca = innerGson.fromJson(jsonObject, Crianca::class.java)
                return ResultData.CriancaResult(crianca)
            } catch (e: Exception) {
                throw JsonParseException("Falha ao desserializar ResultData para Crianca: ${e.message}. JSON: $jsonObject", e)
            }
        }

        // 3. Verificar se é uma Instituição (a instituição tem cnpj, nome_fantasia, etc.)
        else if (jsonObject.has("cnpj") || jsonObject.has("nome_fantasia")) {
            try {
                // CORREÇÃO: Usamos o innerGson para Instituicao
                val instituicao = innerGson.fromJson(jsonObject, Instituicao::class.java)
                return ResultData.InstituicaoResult(instituicao)
            } catch (e: Exception) {
                throw JsonParseException("Falha ao desserializar ResultData para Instituicao: ${e.message}. JSON: $jsonObject", e)
            }
        }

        // Se nenhum tipo for reconhecido, lança exceção.
        throw JsonParseException("Não foi possível determinar o tipo de ResultData a partir do JSON. JSON: $json")
    }
}