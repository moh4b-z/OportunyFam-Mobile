package com.oportunyfam_mobile.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.ErrorResponse
import com.oportunyfam_mobile.model.Publicacao
import com.oportunyfam_mobile.model.PublicacaoCriadaResponse
import com.oportunyfam_mobile.model.PublicacaoRequest
import com.oportunyfam_mobile.model.PublicacoesListResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * ViewModel para gerenciar o estado das publicações
 */
class PublicacaoViewModel : ViewModel() {

    private val publicacaoService = RetrofitFactory().getPublicacaoService()

    // Estado das publicações
    private val _publicacoesState = MutableStateFlow<PublicacoesState>(PublicacoesState.Loading)
    val publicacoesState: StateFlow<PublicacoesState> = _publicacoesState.asStateFlow()

    // Estado de criação de publicação
    private val _criarPublicacaoState = MutableStateFlow<CriarPublicacaoState>(CriarPublicacaoState.Idle)
    val criarPublicacaoState: StateFlow<CriarPublicacaoState> = _criarPublicacaoState.asStateFlow()

    // Estado de edição de publicação
    private val _editarPublicacaoState = MutableStateFlow<EditarPublicacaoState>(EditarPublicacaoState.Idle)
    val editarPublicacaoState: StateFlow<EditarPublicacaoState> = _editarPublicacaoState.asStateFlow()

    /**
     * Buscar publicações por instituição
     */
    fun buscarPublicacoesPorInstituicao(instituicaoId: Int) {
        _publicacoesState.value = PublicacoesState.Loading

        Log.d("PublicacaoViewModel", "🔍 Buscando publicações da instituição ID: $instituicaoId")

        viewModelScope.launch {
            try {
                publicacaoService.buscarPublicacoesPorInstituicao(instituicaoId).enqueue(object : Callback<PublicacoesListResponse> {
                    override fun onResponse(
                        call: Call<PublicacoesListResponse>,
                        response: Response<PublicacoesListResponse>
                    ) {
                        Log.d("PublicacaoViewModel", "📡 Resposta recebida - Código: ${response.code()}")

                        when {
                            response.isSuccessful && response.body() != null -> {
                                val publicacoes = response.body()!!.publicacoes ?: emptyList()
                                Log.d("PublicacaoViewModel", "✅ ${publicacoes.size} publicações carregadas")
                                _publicacoesState.value = PublicacoesState.Success(publicacoes)
                            }
                            response.code() == 404 -> {
                                Log.w("PublicacaoViewModel", "⚠️ Nenhuma publicação encontrada")
                                _publicacoesState.value = PublicacoesState.Success(emptyList())
                            }
                            response.code() == 500 -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("PublicacaoViewModel", "❌ Erro 500: $errorBody")
                                // Tenta buscar todas e filtrar
                                buscarTodasEFiltrar(instituicaoId)
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("PublicacaoViewModel", "❌ Erro ${response.code()}: $errorBody")
                                _publicacoesState.value = PublicacoesState.Error(
                                    "Erro ao carregar publicações (${response.code()})"
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<PublicacoesListResponse>, t: Throwable) {
                        Log.e("PublicacaoViewModel", "❌ Falha na conexão", t)
                        _publicacoesState.value = PublicacoesState.Error(
                            "Erro de conexão: Verifique sua internet"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("PublicacaoViewModel", "❌ Exceção", e)
                _publicacoesState.value = PublicacoesState.Error("Erro inesperado: ${e.message}")
            }
        }
    }

    /**
     * Criar nova publicação (sobrecarga com parâmetros individuais)
     */
    fun criarPublicacao(descricao: String, imagem: String?, instituicaoId: Int) {
        // Limpar espaços extras
        val descricaoLimpa = descricao.trim()

        // Validação de tamanho mínimo
        if (descricaoLimpa.length < 30) {
            _criarPublicacaoState.value = CriarPublicacaoState.Error(
                "A descrição deve ter no mínimo 30 caracteres detalhados (sem contar espaços)"
            )
            return
        }

        // Validação de tamanho máximo (provavelmente 500 caracteres no backend)
        if (descricaoLimpa.length > 500) {
            _criarPublicacaoState.value = CriarPublicacaoState.Error(
                "A descrição não pode ter mais de 500 caracteres (atual: ${descricaoLimpa.length})"
            )
            return
        }

        // Log de debug
        Log.d("PublicacaoViewModel", "Validação OK - Descrição: ${descricaoLimpa.length} chars")

        if (imagem.isNullOrBlank()) {
            _criarPublicacaoState.value = CriarPublicacaoState.Error(
                "É necessário selecionar uma imagem"
            )
            return
        }

        val request = PublicacaoRequest(
            descricao = descricaoLimpa,
            imagem = imagem,
            instituicaoId = instituicaoId
        )
        criarPublicacao(request)
    }

    /**
     * Criar nova publicação
     */
    fun criarPublicacao(request: PublicacaoRequest) {
        _criarPublicacaoState.value = CriarPublicacaoState.Loading

        Log.d("PublicacaoViewModel", "📝 Criando publicação")
        Log.d("PublicacaoViewModel", "📋 Dados do request:")
        Log.d("PublicacaoViewModel", "  ➤ Descrição: '${request.descricao}' (${request.descricao.length} chars)")
        Log.d("PublicacaoViewModel", "  ➤ Imagem: ${if (request.imagem?.isNotEmpty() == true) "✅ ${request.imagem}" else "❌"}")
        Log.d("PublicacaoViewModel", "  ➤ Instituição ID: ${request.instituicaoId}")

        // Log JSON payload
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(request)
            Log.d("PublicacaoViewModel", "📦 JSON payload: $json")
        } catch (e: Exception) {
            Log.e("PublicacaoViewModel", "Erro ao serializar JSON", e)
        }

        viewModelScope.launch {
            try {
                publicacaoService.criarPublicacao(request).enqueue(object : Callback<PublicacaoCriadaResponse> {
                    override fun onResponse(
                        call: Call<PublicacaoCriadaResponse>,
                        response: Response<PublicacaoCriadaResponse>
                    ) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                Log.d("PublicacaoViewModel", "✅ Publicação criada com sucesso!")
                                _criarPublicacaoState.value = CriarPublicacaoState.Success(
                                    response.body()!!.publicacao_instituicao
                                )
                                // Recarregar lista
                                if (response.body()!!.publicacao_instituicao != null) {
                                    buscarPublicacoesPorInstituicao(request.instituicaoId)
                                }
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("PublicacaoViewModel", "❌ Erro ao criar: $errorBody")

                                // Extrair mensagem específica se possível
                                val mensagemErro = try {
                                    val jsonError = com.google.gson.Gson().fromJson(
                                        errorBody,
                                        ErrorResponse::class.java
                                    )
                                    jsonError?.messagem ?: "Erro ao criar publicação (${response.code()})"
                                } catch (e: Exception) {
                                    "Erro ao criar publicação (${response.code()})"
                                }

                                _criarPublicacaoState.value = CriarPublicacaoState.Error(mensagemErro)
                            }
                        }
                    }

                    override fun onFailure(call: Call<PublicacaoCriadaResponse>, t: Throwable) {
                        Log.e("PublicacaoViewModel", "❌ Falha ao criar", t)
                        _criarPublicacaoState.value = CriarPublicacaoState.Error(
                            "Erro de conexão: ${t.message}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("PublicacaoViewModel", "❌ Exceção ao criar", e)
                _criarPublicacaoState.value = CriarPublicacaoState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Deletar publicação
     */
    fun deletarPublicacao(publicacaoId: Int, instituicaoId: Int) {
        Log.d("PublicacaoViewModel", "🗑️ Deletando publicação ID: $publicacaoId")

        viewModelScope.launch {
            try {
                publicacaoService.deletarPublicacao(publicacaoId).enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            Log.d("PublicacaoViewModel", "✅ Publicação deletada")
                            // Recarregar lista
                            buscarPublicacoesPorInstituicao(instituicaoId)
                        } else {
                            Log.e("PublicacaoViewModel", "❌ Erro ao deletar: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.e("PublicacaoViewModel", "❌ Falha ao deletar", t)
                    }
                })
            } catch (e: Exception) {
                Log.e("PublicacaoViewModel", "❌ Exceção ao deletar", e)
            }
        }
    }

    /**
     * Editar publicação existente
     */
    fun editarPublicacao(publicacaoId: Int, descricao: String, imagem: String?, instituicaoId: Int) {
        // Limpar espaços extras
        val descricaoLimpa = descricao.trim()

        // Validação de tamanho mínimo
        if (descricaoLimpa.length < 30) {
            _editarPublicacaoState.value = EditarPublicacaoState.Error(
                "A descrição deve ter no mínimo 30 caracteres detalhados"
            )
            return
        }

        // Validação de tamanho máximo
        if (descricaoLimpa.length > 500) {
            _editarPublicacaoState.value = EditarPublicacaoState.Error(
                "A descrição não pode ter mais de 500 caracteres (atual: ${descricaoLimpa.length})"
            )
            return
        }

        if (imagem.isNullOrBlank()) {
            _editarPublicacaoState.value = EditarPublicacaoState.Error(
                "É necessário ter uma imagem"
            )
            return
        }

        _editarPublicacaoState.value = EditarPublicacaoState.Loading

        Log.d("PublicacaoViewModel", "✏️ Editando publicação ID: $publicacaoId")
        Log.d("PublicacaoViewModel", "📋 Dados do request:")
        Log.d("PublicacaoViewModel", "  ➤ Descrição: '${descricaoLimpa}' (${descricaoLimpa.length} chars)")
        Log.d("PublicacaoViewModel", "  ➤ Imagem: ${if (imagem.isNotEmpty()) "✅ $imagem" else "❌"}")

        val request = PublicacaoRequest(
            descricao = descricaoLimpa,
            imagem = imagem,
            instituicaoId = instituicaoId
        )

        // Log JSON payload
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(request)
            Log.d("PublicacaoViewModel", "📦 JSON payload: $json")
        } catch (e: Exception) {
            Log.e("PublicacaoViewModel", "Erro ao serializar JSON", e)
        }

        viewModelScope.launch {
            try {
                publicacaoService.atualizarPublicacao(publicacaoId, request).enqueue(object : Callback<PublicacaoCriadaResponse> {
                    override fun onResponse(
                        call: Call<PublicacaoCriadaResponse>,
                        response: Response<PublicacaoCriadaResponse>
                    ) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                Log.d("PublicacaoViewModel", "✅ Publicação editada com sucesso!")
                                _editarPublicacaoState.value = EditarPublicacaoState.Success(
                                    response.body()!!.publicacao_instituicao
                                )
                                // Recarregar lista
                                buscarPublicacoesPorInstituicao(instituicaoId)
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("PublicacaoViewModel", "❌ Erro ao editar: $errorBody")

                                val mensagemErro = try {
                                    val jsonError = com.google.gson.Gson().fromJson(
                                        errorBody,
                                        ErrorResponse::class.java
                                    )
                                    jsonError?.messagem ?: "Erro ao editar publicação (${response.code()})"
                                } catch (e: Exception) {
                                    "Erro ao editar publicação (${response.code()})"
                                }

                                _editarPublicacaoState.value = EditarPublicacaoState.Error(mensagemErro)
                            }
                        }
                    }

                    override fun onFailure(call: Call<PublicacaoCriadaResponse>, t: Throwable) {
                        Log.e("PublicacaoViewModel", "❌ Falha ao editar", t)
                        _editarPublicacaoState.value = EditarPublicacaoState.Error(
                            "Erro de conexão: ${t.message}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("PublicacaoViewModel", "❌ Exceção ao editar", e)
                _editarPublicacaoState.value = EditarPublicacaoState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Limpar estado de criação
     */
    fun limparEstadoCriacao() {
        _criarPublicacaoState.value = CriarPublicacaoState.Idle
    }

    /**
     * Limpar estado de edição
     */
    fun limparEstadoEdicao() {
        _editarPublicacaoState.value = EditarPublicacaoState.Idle
    }

    /**
     * Fallback: busca todas as publicações e filtra pela instituição
     */
    private fun buscarTodasEFiltrar(instituicaoId: Int) {
        viewModelScope.launch {
            try {
                publicacaoService.buscarTodasPublicacoes().enqueue(object : Callback<PublicacoesListResponse> {
                    override fun onResponse(
                        call: Call<PublicacoesListResponse>,
                        response: Response<PublicacoesListResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val todasPublicacoes = response.body()!!.publicacoes ?: emptyList()
                            val publicacoesFiltradas = todasPublicacoes.filter {
                                it.id_instituicao == instituicaoId
                            }

                            Log.d("PublicacaoViewModel", "✅ Fallback: ${publicacoesFiltradas.size} publicações encontradas")
                            _publicacoesState.value = PublicacoesState.Success(publicacoesFiltradas)
                        } else {
                            Log.e("PublicacaoViewModel", "❌ Fallback falhou: ${response.code()}")
                            _publicacoesState.value = PublicacoesState.Success(emptyList())
                        }
                    }

                    override fun onFailure(call: Call<PublicacoesListResponse>, t: Throwable) {
                        Log.e("PublicacaoViewModel", "❌ Fallback falhou na conexão", t)
                        _publicacoesState.value = PublicacoesState.Success(emptyList())
                    }
                })
            } catch (e: Exception) {
                Log.e("PublicacaoViewModel", "❌ Exceção no fallback", e)
                _publicacoesState.value = PublicacoesState.Success(emptyList())
            }
        }
    }
}

/**
 * Estados possíveis para a lista de publicações
 */
sealed class PublicacoesState {
    object Loading : PublicacoesState()
    data class Success(val publicacoes: List<Publicacao>) : PublicacoesState()
    data class Error(val message: String) : PublicacoesState()
}

/**
 * Estados possíveis para criar publicação
 */
sealed class CriarPublicacaoState {
    object Idle : CriarPublicacaoState()
    object Loading : CriarPublicacaoState()
    data class Success(val publicacao: Publicacao?) : CriarPublicacaoState()
    data class Error(val message: String) : CriarPublicacaoState()
}

/**
 * Estados possíveis para editar publicação
 */
sealed class EditarPublicacaoState {
    object Idle : EditarPublicacaoState()
    object Loading : EditarPublicacaoState()
    data class Success(val publicacao: Publicacao?) : EditarPublicacaoState()
    data class Error(val message: String) : EditarPublicacaoState()
}