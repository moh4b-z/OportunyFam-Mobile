package com.oportunyfam_mobile.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel para gerenciar o estado das aulas
 */
class AulaViewModel : ViewModel() {

    private val atividadeService = RetrofitFactory().getAtividadeService()

    // Estado das aulas por atividade
    private val _aulasState = MutableStateFlow<AulasState>(AulasState.Idle)
    val aulasState: StateFlow<AulasState> = _aulasState.asStateFlow()

    // Estado de criação de aula
    private val _criarAulaState = MutableStateFlow<CriarAulaState>(CriarAulaState.Idle)
    val criarAulaState: StateFlow<CriarAulaState> = _criarAulaState.asStateFlow()

    /**
     * Buscar aulas de uma atividade específica
     * (Como não há endpoint específico, busca pela instituição e filtra)
     */
    fun buscarAulasPorAtividade(atividadeId: Int, instituicaoId: Int) {
        _aulasState.value = AulasState.Loading

        Log.d("AulaViewModel", "🔍 Buscando aulas da atividade ID: $atividadeId")

        viewModelScope.launch {
            try {
                atividadeService.buscarAulasPorInstituicao(instituicaoId).enqueue(object : Callback<AulasListResponse> {
                    override fun onResponse(
                        call: Call<AulasListResponse>,
                        response: Response<AulasListResponse>
                    ) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                val todasAulas = response.body()!!.aulas ?: emptyList()
                                Log.d("AulaViewModel", "📊 Total de aulas da instituição: ${todasAulas.size}")

                                // Filtrar apenas as aulas da atividade específica
                                val aulasFiltradas = todasAulas.filter { it.id_atividade == atividadeId }

                                Log.d("AulaViewModel", "✅ ${aulasFiltradas.size} aulas carregadas para atividade $atividadeId")

                                // Log detalhado de cada aula
                                aulasFiltradas.forEach { aula ->
                                    Log.d("AulaViewModel", "  📅 Aula ID ${aula.aula_id}: ${aula.data_aula} ${aula.hora_inicio}-${aula.hora_fim}")
                                }

                                _aulasState.value = AulasState.Success(aulasFiltradas)
                            }
                            response.code() == 404 -> {
                                // 404 significa que não há aulas cadastradas ainda
                                Log.d("AulaViewModel", "ℹ️ Nenhuma aula encontrada (404) - retornando lista vazia")
                                _aulasState.value = AulasState.Success(emptyList())
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("AulaViewModel", "❌ Erro ${response.code()}: $errorBody")
                                _aulasState.value = AulasState.Error("Erro ao carregar aulas (${response.code()})")
                            }
                        }
                    }

                    override fun onFailure(call: Call<AulasListResponse>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Falha na conexão: ${t.message}", t)
                        _aulasState.value = AulasState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção: ${e.message}", e)
                _aulasState.value = AulasState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Criar uma aula individual
     */
    fun criarAula(aulaRequest: AulaRequest) {
        _criarAulaState.value = CriarAulaState.Loading

        Log.d("AulaViewModel", "📝 Criando aula para ${aulaRequest.data_aula}")

        viewModelScope.launch {
            try {
                atividadeService.criarAula(aulaRequest).enqueue(object : Callback<AulaCriadaResponse> {
                    override fun onResponse(
                        call: Call<AulaCriadaResponse>,
                        response: Response<AulaCriadaResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            Log.d("AulaViewModel", "✅ Aula criada com sucesso!")
                            _criarAulaState.value = CriarAulaState.Success(response.body()!!.aula)
                        } else {
                            Log.e("AulaViewModel", "❌ Erro ao criar: ${response.errorBody()?.string()}")
                            _criarAulaState.value = CriarAulaState.Error("Erro ao criar aula (${response.code()})")
                        }
                    }

                    override fun onFailure(call: Call<AulaCriadaResponse>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Falha ao criar", t)
                        _criarAulaState.value = CriarAulaState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção ao criar", e)
                _criarAulaState.value = CriarAulaState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Criar várias aulas de uma vez (lote)
     */
    fun criarAulasLote(aulaLoteRequest: AulaLoteRequest) {
        _criarAulaState.value = CriarAulaState.Loading

        Log.d("AulaViewModel", "📝 Criando ${aulaLoteRequest.datas.size} aulas em lote")

        viewModelScope.launch {
            try {
                atividadeService.criarAulasLote(aulaLoteRequest).enqueue(object : Callback<AulaLoteResponse> {
                    override fun onResponse(
                        call: Call<AulaLoteResponse>,
                        response: Response<AulaLoteResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val total = response.body()!!.total_inseridas ?: 0
                            Log.d("AulaViewModel", "✅ $total aulas criadas com sucesso!")
                            _criarAulaState.value = CriarAulaState.SuccessLote(
                                response.body()!!.aulas_inseridas ?: emptyList(),
                                total
                            )
                        } else {
                            Log.e("AulaViewModel", "❌ Erro ao criar: ${response.errorBody()?.string()}")
                            _criarAulaState.value = CriarAulaState.Error("Erro ao criar aulas (${response.code()})")
                        }
                    }

                    override fun onFailure(call: Call<AulaLoteResponse>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Falha ao criar", t)
                        _criarAulaState.value = CriarAulaState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção ao criar", e)
                _criarAulaState.value = CriarAulaState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Deletar uma aula
     */
    fun deletarAula(aulaId: Int) {
        Log.d("AulaViewModel", "🗑️ Deletando aula ID: $aulaId")

        viewModelScope.launch {
            try {
                atividadeService.deletarAula(aulaId).enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            Log.d("AulaViewModel", "✅ Aula deletada com sucesso!")
                        } else {
                            Log.e("AulaViewModel", "❌ Erro ao deletar: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Falha ao deletar", t)
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção ao deletar", e)
            }
        }
    }

    /**
     * Limpar estado de criação
     */
    fun limparEstadoCriacao() {
        _criarAulaState.value = CriarAulaState.Idle
    }

    /**
     * Recarregar aulas após criar/deletar
     */
    fun recarregarAulas(atividadeId: Int, instituicaoId: Int) {
        Log.d("AulaViewModel", "🔄 Iniciando recarregamento de aulas...")
        buscarAulasPorAtividade(atividadeId, instituicaoId)
    }

    /**
     * Buscar todas as aulas (fallback) e filtrar por atividade
     */
    fun buscarTodasAulasEFiltrar(atividadeId: Int) {
        _aulasState.value = AulasState.Loading

        Log.d("AulaViewModel", "🔍 Fallback: Buscando TODAS as aulas e filtrando...")

        viewModelScope.launch {
            try {
                atividadeService.buscarTodasAulas().enqueue(object : Callback<AulasListResponse> {
                    override fun onResponse(
                        call: Call<AulasListResponse>,
                        response: Response<AulasListResponse>
                    ) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                val todasAulas = response.body()!!.aulas ?: emptyList()
                                val aulasFiltradas = todasAulas.filter { it.id_atividade == atividadeId }

                                Log.d("AulaViewModel", "✅ Fallback: ${aulasFiltradas.size} aulas encontradas")
                                _aulasState.value = AulasState.Success(aulasFiltradas)
                            }
                            response.code() == 404 -> {
                                Log.d("AulaViewModel", "ℹ️ Fallback: Nenhuma aula (404)")
                                _aulasState.value = AulasState.Success(emptyList())
                            }
                            else -> {
                                Log.e("AulaViewModel", "❌ Fallback falhou: ${response.code()}")
                                _aulasState.value = AulasState.Error("Erro ao buscar aulas")
                            }
                        }
                    }

                    override fun onFailure(call: Call<AulasListResponse>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Fallback falhou: ${t.message}", t)
                        _aulasState.value = AulasState.Error("Erro de conexão")
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção no fallback", e)
                _aulasState.value = AulasState.Error("Erro inesperado")
            }
        }
    }
}

/**
 * Estados possíveis para a lista de aulas
 */
sealed class AulasState {
    object Idle : AulasState()
    object Loading : AulasState()
    data class Success(val aulas: List<AulaDetalhada>) : AulasState()
    data class Error(val message: String) : AulasState()
}

/**
 * Estados possíveis para criação de aula
 */
sealed class CriarAulaState {
    object Idle : CriarAulaState()
    object Loading : CriarAulaState()
    data class Success(val aula: AulaSimples?) : CriarAulaState()
    data class SuccessLote(val aulas: List<AulaSimples>, val total: Int) : CriarAulaState()
    data class Error(val message: String) : CriarAulaState()
}

