package com.oportunyfam_mobile_ong.model


import com.google.gson.annotations.SerializedName

// Esta classe representa o objeto JSON completo retornado pela API
data class TipoInstituicaoResponse(
    val status: Boolean,
    @SerializedName("status_code")
    val statusCode: Int,
    val messagem: String,
    @SerializedName("tipos_instituicao")
    val tiposInstituicao: List<TipoInstituicao>
)
data class TipoInstituicao(
    val id: Int? = null, // Pode ser nulo no POST
    val nome: String
)