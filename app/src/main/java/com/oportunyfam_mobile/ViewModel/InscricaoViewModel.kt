package com.oportunyfam_mobile.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.InscricaoDetalhada
import com.oportunyfam_mobile.model.InscricoesResponse
import com.oportunyfam_mobile.model.InscricaoUpdateRequest
import com.oportunyfam_mobile.model.InscricaoCriadaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel para gerenciar inscrições de alunos nas atividades
 */
class InscricaoViewModel : ViewModel() {

    private val inscricaoService = RetrofitFactory().getInscricaoService()

    // Estado das inscrições
    private val _inscricoesState = MutableStateFlow<InscricoesState>(InscricoesState.Loading)
    val inscricoesState: StateFlow<InscricoesState> = _inscricoesState.asStateFlow()

    // Estado de atualização
    private val _atualizarState = MutableStateFlow<AtualizarInscricaoState>(AtualizarInscricaoState.Idle)
    val atualizarState: StateFlow<AtualizarInscricaoState> = _atualizarState.asStateFlow()

    /**
     * Buscar inscrições por atividade
     */
    fun buscarInscricoesPorAtividade(atividadeId: Int) {
        _inscricoesState.value = InscricoesState.Loading

        Log.d("InscricaoViewModel", "🔍 Buscando inscrições da atividade ID: $atividadeId")

        viewModelScope.launch {
            try {
                // Tentar endpoint específico primeiro
                inscricaoService.buscarInscricoesPorAtividade(atividadeId).enqueue(object : Callback<InscricoesResponse> {
                    override fun onResponse(
                        call: Call<InscricoesResponse>,
                        response: Response<InscricoesResponse>
                    ) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                val inscricoes = response.body()!!.inscricoes ?: emptyList()
                                Log.d("InscricaoViewModel", "✅ ${inscricoes.size} inscrições carregadas")
                                _inscricoesState.value = InscricoesState.Success(inscricoes)
                            }
                            response.code() == 404 -> {
                                // Endpoint não existe, usar fallback
                                Log.w("InscricaoViewModel", "⚠️ Endpoint /atividade/:id não existe, usando fallback")
                                buscarTodasEFiltrar(atividadeId)
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("InscricaoViewModel", "❌ Erro ${response.code()}: $errorBody")
                                // Tentar fallback em caso de erro
                                buscarTodasEFiltrar(atividadeId)
                            }
                        }
                    }

                    override fun onFailure(call: Call<InscricoesResponse>, t: Throwable) {
                        Log.e("InscricaoViewModel", "❌ Falha na conexão, tentando fallback", t)
                        // Tentar fallback
                        buscarTodasEFiltrar(atividadeId)
                    }
                })
            } catch (e: Exception) {
                Log.e("InscricaoViewModel", "❌ Exceção", e)
                _inscricoesState.value = InscricoesState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Fallback: Buscar todas as inscrições e filtrar pela atividade localmente
     */
    private fun buscarTodasEFiltrar(atividadeId: Int) {
        Log.d("InscricaoViewModel", "🔄 Usando fallback: buscando todas as inscrições")

        viewModelScope.launch {
            try {
                inscricaoService.buscarTodasInscricoes().enqueue(object : Callback<InscricoesResponse> {
                    override fun onResponse(
                        call: Call<InscricoesResponse>,
                        response: Response<InscricoesResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val todasInscricoes = response.body()!!.inscricoes ?: emptyList()
                            Log.d("InscricaoViewModel", "📊 Total de inscrições da API: ${todasInscricoes.size}")

                            // Log das atividades encontradas
                            todasInscricoes.forEach { inscricao ->
                                Log.d("InscricaoViewModel", "   - Inscrição ID ${inscricao.inscricao_id}: atividade_id=${inscricao.atividade_id}, criança=${inscricao.crianca_nome}")
                            }

                            Log.d("InscricaoViewModel", "🔍 Filtrando por atividade_id = $atividadeId")

                            val inscricoesFiltradas = todasInscricoes.filter {
                                it.atividade_id == atividadeId
                            }

                            Log.d("InscricaoViewModel", "✅ Fallback: ${inscricoesFiltradas.size} inscrições encontradas para atividade $atividadeId")
                            _inscricoesState.value = InscricoesState.Success(inscricoesFiltradas)
                        } else {
                            Log.e("InscricaoViewModel", "❌ Fallback falhou: ${response.code()}")
                            _inscricoesState.value = InscricoesState.Success(emptyList())
                        }
                    }

                    override fun onFailure(call: Call<InscricoesResponse>, t: Throwable) {
                        Log.e("InscricaoViewModel", "❌ Fallback falhou na conexão", t)
                        _inscricoesState.value = InscricoesState.Error(
                            "Erro de conexão: ${t.message}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("InscricaoViewModel", "❌ Exceção no fallback", e)
                _inscricoesState.value = InscricoesState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Atualizar status da inscrição
     */
    fun atualizarStatusInscricao(inscricaoId: Int, novoStatus: Int, atividadeId: Int) {
        Log.e("InscricaoViewModel", "==========================================")
        Log.e("InscricaoViewModel", "🚨 MÉTODO atualizarStatusInscricao CHAMADO!")
        Log.e("InscricaoViewModel", "Parâmetros recebidos:")
        Log.e("InscricaoViewModel", "  - inscricaoId: $inscricaoId")
        Log.e("InscricaoViewModel", "  - novoStatus: $novoStatus")
        Log.e("InscricaoViewModel", "  - atividadeId: $atividadeId")
        Log.e("InscricaoViewModel", "==========================================")

        _atualizarState.value = AtualizarInscricaoState.Loading

        Log.d("InscricaoViewModel", "✏️ Atualizando inscrição ID: $inscricaoId para status: $novoStatus")
        Log.d("InscricaoViewModel", "📤 Enviando para: PUT /inscricoes/$inscricaoId")

        val request = InscricaoUpdateRequest(id_status = novoStatus)
        Log.d("InscricaoViewModel", "📦 Request body: {\"id_status\": $novoStatus}")
        Log.d("InscricaoViewModel", "🔧 InscricaoService: ${inscricaoService.javaClass.simpleName}")

        viewModelScope.launch {
            try {
                Log.d("InscricaoViewModel", "🚀 Prestes a chamar inscricaoService.atualizarInscricao($inscricaoId, request)")
                val call = inscricaoService.atualizarInscricao(inscricaoId, request)
                Log.d("InscricaoViewModel", "📞 Call criado: ${call.request().url}")
                call.enqueue(object : Callback<InscricaoCriadaResponse> {
                    override fun onResponse(
                        call: Call<InscricaoCriadaResponse>,
                        response: Response<InscricaoCriadaResponse>
                    ) {
                        Log.d("InscricaoViewModel", "📥 Response code: ${response.code()}")

                        when {
                            response.isSuccessful && response.body() != null -> {
                                val body = response.body()!!
                                Log.d("InscricaoViewModel", "✅ Status atualizado com sucesso")
                                Log.d("InscricaoViewModel", "📋 Response: status=${body.status}, message=${body.messagem}")

                                if (body.inscricao != null) {
                                    Log.d("InscricaoViewModel", "📄 Inscrição atualizada: id=${body.inscricao.id}, id_status=${body.inscricao.id_status}")
                                }

                                _atualizarState.value = AtualizarInscricaoState.Success
                                // Recarregar lista
                                buscarInscricoesPorAtividade(atividadeId)
                            }
                            response.isSuccessful -> {
                                Log.w("InscricaoViewModel", "⚠️ Sucesso mas body vazio")
                                _atualizarState.value = AtualizarInscricaoState.Success
                                buscarInscricoesPorAtividade(atividadeId)
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("InscricaoViewModel", "❌ Erro ao atualizar: ${response.code()}")
                                Log.e("InscricaoViewModel", "❌ Error body: $errorBody")
                                _atualizarState.value = AtualizarInscricaoState.Error(
                                    "Erro ao atualizar status (${response.code()})"
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<InscricaoCriadaResponse>, t: Throwable) {
                        Log.e("InscricaoViewModel", "❌ Falha ao atualizar", t)
                        _atualizarState.value = AtualizarInscricaoState.Error(
                            "Erro de conexão: ${t.message}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("InscricaoViewModel", "❌ Exceção ao atualizar", e)
                _atualizarState.value = AtualizarInscricaoState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Remover aluno (deletar inscrição)
     */
    fun removerAluno(inscricaoId: Int, atividadeId: Int) {
        Log.d("InscricaoViewModel", "🗑️ Removendo inscrição ID: $inscricaoId")

        viewModelScope.launch {
            try {
                inscricaoService.deletarInscricao(inscricaoId).enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            Log.d("InscricaoViewModel", "✅ Aluno removido com sucesso")
                            // Recarregar lista
                            buscarInscricoesPorAtividade(atividadeId)
                        } else {
                            Log.e("InscricaoViewModel", "❌ Erro ao remover: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.e("InscricaoViewModel", "❌ Falha ao remover", t)
                    }
                })
            } catch (e: Exception) {
                Log.e("InscricaoViewModel", "❌ Exceção ao remover", e)
            }
        }
    }

    /**
     * Limpar estado de atualização
     */
    fun limparEstadoAtualizacao() {
        _atualizarState.value = AtualizarInscricaoState.Idle
    }
}

/**
 * Estados possíveis para a lista de inscrições
 */
sealed class InscricoesState {
    object Loading : InscricoesState()
    data class Success(val inscricoes: List<InscricaoDetalhada>) : InscricoesState()
    data class Error(val message: String) : InscricoesState()
}

/**
 * Estados possíveis para atualizar inscrição
 */
sealed class AtualizarInscricaoState {
    object Idle : AtualizarInscricaoState()
    object Loading : AtualizarInscricaoState()
    object Success : AtualizarInscricaoState()
    data class Error(val message: String) : AtualizarInscricaoState()
}

