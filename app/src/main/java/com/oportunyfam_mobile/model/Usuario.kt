package com.oportunyfam_mobile.model

import com.google.gson.annotations.SerializedName
import com.oportunyfam_mobile.model.Crianca

data class UsuarioResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val usuario: Usuario?
)

/**
 * ALTERAÇÕES REALIZADAS EM Usuario:
 *
 * Adicionadas anotações @SerializedName em TODOS os campos para mapear corretamente
 * os nomes do JSON retornado pela API para os campos Kotlin.
 *
 * PROBLEMA QUE ESTAVA ACONTECENDO:
 * - O campo 'id' estava recebendo valor 0 (padrão) porque o Gson não estava
 *   conseguindo desserializar corretamente do JSON para o objeto Kotlin
 * - Isso fazia com que a requisição PUT fosse enviada para /v1/usuario/0
 *   resultando em erro 404
 *
 * SOLUÇÃO:
 * - @SerializedName("id") garante que o valor do campo "id" no JSON
 *   seja mapeado para o campo "id" no objeto Usuario
 * - Sem isso, o Gson tentava adivinhar automaticamente o mapeamento
 *
 * EXEMPLO:
 * JSON retornado pela API: { "id": 123, "nome": "João", ... }
 *
 * SEM @SerializedName: id continuaria como 0 (valor padrão)
 * COM @SerializedName("id"): id recebe corretamente o valor 123
 */
data class Usuario(
    @SerializedName("usuario_id") // Mapeia "usuario_id" do JSON para este campo
    val usuario_id: Int,
    @SerializedName("pessoa_id") // Mapeia "pessoa_id" do JSON para este campo
    val pessoa_id: Int,
    @SerializedName("nome") // Mapeia "nome" do JSON para este campo
    val nome: String,
    @SerializedName("foto_perfil") // Mapeia "foto_perfil" do JSON para este campo
    val foto_perfil: String?,
    @SerializedName("email") // Mapeia "email" do JSON para este campo
    val email: String,
    @SerializedName("data_nascimento") // Mapeia "data_nascimento" do JSON para este campo
    val data_nascimento: String,
    @SerializedName("cpf") // Mapeia "cpf" do JSON para este campo
    val cpf: String,
    @SerializedName("criado_em") // Mapeia "criado_em" do JSON para este campo
    val criado_em: String,
    @SerializedName("telefone") // Mapeia "telefone" do JSON para este campo
    val telefone: String?,
    @SerializedName("atualizado_em") // Mapeia "atualizado_em" do JSON para este campo
    val atualizado_em: String?,
    @SerializedName("sexo") // Mapeia "sexo" do JSON para este campo
    val sexo: String?,
    @SerializedName("tipo_nivel") // Mapeia "tipo_nivel" do JSON para este campo
    val tipo_nivel: String?,
    @SerializedName("criancas_dependentes") // Mapeia "criancas_dependentes" do JSON para este campo
    val criancas_dependentes: List<CriancaMini> = emptyList(),
    @SerializedName("conversas") // Mapeia "conversas" do JSON para este campo
    val conversas: List<Any> = emptyList(),
    @SerializedName("id") // CRÍTICO: Mapeia "id" do JSON (este era o campo que vinha como 0 antes)
    val id: Int // ID único que será usado na requisição PUT
)

data class UsuarioRequest(
    val nome: String,
    val foto_perfil: String?,
    val email: String,
    val senha: String,
    val data_nascimento: String,
    val telefone: String?,
    val cpf: String,
    val id_sexo: Int,
    val id_tipo_nivel: Int,
    val cep: String,
    val logradouro: String,
    val numero: String?,
    val complemento: String?,
    val bairro: String,
    val cidade: String,
    val estado: String
)