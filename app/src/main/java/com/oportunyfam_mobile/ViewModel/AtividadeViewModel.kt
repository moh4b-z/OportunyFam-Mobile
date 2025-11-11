package com.oportunyfam_mobile_ong.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile_ong.Service.RetrofitFactory
import com.oportunyfam_mobile_ong.model.AtividadeResponse
import com.oportunyfam_mobile_ong.model.AtividadesListResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel para gerenciar o estado das atividades
 */
class AtividadeViewModel : ViewModel() {

    private val atividadeService = RetrofitFactory().getAtividadeService()

    // Estado das atividades
    private val _atividadesState = MutableStateFlow<AtividadesState>(AtividadesState.Loading)
    val atividadesState: StateFlow<AtividadesState> = _atividadesState.asStateFlow()

    // Estado de uma atividade específica
    private val _atividadeDetalheState = MutableStateFlow<AtividadeDetalheState>(AtividadeDetalheState.Idle)
    val atividadeDetalheState: StateFlow<AtividadeDetalheState> = _atividadeDetalheState.asStateFlow()

    /**
     * Buscar todas as atividades
     */
    fun buscarTodasAtividades() {
        _atividadesState.value = AtividadesState.Loading

        viewModelScope.launch {
            try {
                atividadeService.buscarTodasAtividades().enqueue(object : Callback<AtividadesListResponse> {
                    override fun onResponse(
                        call: Call<AtividadesListResponse>,
                        response: Response<AtividadesListResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val atividades = response.body()!!.atividades
                            Log.d("AtividadeViewModel", "Atividades carregadas: ${atividades.size}")
                            _atividadesState.value = AtividadesState.Success(atividades)
                        } else {
                            Log.e("AtividadeViewModel", "Erro na resposta: ${response.code()}")
                            _atividadesState.value = AtividadesState.Error("Erro ao carregar atividades: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<AtividadesListResponse>, t: Throwable) {
                        Log.e("AtividadeViewModel", "Falha na requisição", t)
                        _atividadesState.value = AtividadesState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AtividadeViewModel", "Exceção ao buscar atividades", e)
                _atividadesState.value = AtividadesState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Buscar atividades por instituição
     */
    fun buscarAtividadesPorInstituicao(instituicaoId: Int) {
        _atividadesState.value = AtividadesState.Loading

        Log.d("AtividadeViewModel", "🔍 Buscando atividades da instituição ID: $instituicaoId")

        viewModelScope.launch {
            try {
                atividadeService.buscarAtividadesPorInstituicao(instituicaoId).enqueue(object : Callback<AtividadesListResponse> {
                    override fun onResponse(
                        call: Call<AtividadesListResponse>,
                        response: Response<AtividadesListResponse>
                    ) {
                        Log.d("AtividadeViewModel", "📡 Resposta recebida - Código: ${response.code()}")

                        when {
                            response.isSuccessful && response.body() != null -> {
                                val atividades = response.body()!!.atividades
                                Log.d("AtividadeViewModel", "✅ ${atividades.size} atividades carregadas")
                                _atividadesState.value = AtividadesState.Success(atividades)
                            }
                            response.code() == 500 -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("AtividadeViewModel", "❌ Erro 500 do servidor")
                                Log.e("AtividadeViewModel", "Resposta: $errorBody")

                                // Tentar buscar todas as atividades como fallback
                                Log.d("AtividadeViewModel", "🔄 Tentando fallback: buscar todas e filtrar localmente")
                                buscarTodasEFiltrar(instituicaoId)
                            }
                            response.code() == 404 -> {
                                Log.w("AtividadeViewModel", "⚠️ Instituição não encontrada ou sem atividades")
                                _atividadesState.value = AtividadesState.Success(emptyList())
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("AtividadeViewModel", "❌ Erro ${response.code()}: $errorBody")
                                _atividadesState.value = AtividadesState.Error(
                                    "Erro ao carregar atividades (${response.code()})"
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<AtividadesListResponse>, t: Throwable) {
                        Log.e("AtividadeViewModel", "❌ Falha na conexão: ${t.message}", t)
                        _atividadesState.value = AtividadesState.Error(
                            "Erro de conexão: Verifique sua internet"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("AtividadeViewModel", "❌ Exceção: ${e.message}", e)
                _atividadesState.value = AtividadesState.Error("Erro inesperado: ${e.message}")
            }
        }
    }

    /**
     * Buscar detalhes de uma atividade específica
     */
    fun buscarAtividadePorId(atividadeId: Int) {
        _atividadeDetalheState.value = AtividadeDetalheState.Loading

        viewModelScope.launch {
            try {
                atividadeService.buscarAtividadePorId(atividadeId).enqueue(object : Callback<AtividadeResponse> {
                    override fun onResponse(
                        call: Call<AtividadeResponse>,
                        response: Response<AtividadeResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val atividade = response.body()!!
                            Log.d("AtividadeViewModel", "Atividade carregada: ${atividade.titulo}")
                            _atividadeDetalheState.value = AtividadeDetalheState.Success(atividade)
                        } else {
                            Log.e("AtividadeViewModel", "Erro na resposta: ${response.code()}")
                            _atividadeDetalheState.value = AtividadeDetalheState.Error("Erro ao carregar atividade: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<AtividadeResponse>, t: Throwable) {
                        Log.e("AtividadeViewModel", "Falha na requisição", t)
                        _atividadeDetalheState.value = AtividadeDetalheState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AtividadeViewModel", "Exceção ao buscar atividade", e)
                _atividadeDetalheState.value = AtividadeDetalheState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Limpar estado de detalhe
     */
    fun limparDetalhe() {
        _atividadeDetalheState.value = AtividadeDetalheState.Idle
    }

    /**
     * Fallback: busca todas as atividades e filtra pela instituição localmente
     */
    private fun buscarTodasEFiltrar(instituicaoId: Int) {
        viewModelScope.launch {
            try {
                atividadeService.buscarTodasAtividades().enqueue(object : Callback<AtividadesListResponse> {
                    override fun onResponse(
                        call: Call<AtividadesListResponse>,
                        response: Response<AtividadesListResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val todasAtividades = response.body()!!.atividades
                            val atividadesFiltradas = todasAtividades.filter {
                                it.instituicao_id == instituicaoId
                            }

                            Log.d("AtividadeViewModel", "✅ Fallback: ${atividadesFiltradas.size} atividades encontradas")
                            _atividadesState.value = AtividadesState.Success(atividadesFiltradas)
                        } else {
                            Log.e("AtividadeViewModel", "❌ Fallback também falhou: ${response.code()}")
                            _atividadesState.value = AtividadesState.Error(
                                "Não foi possível carregar as atividades. Tente novamente mais tarde."
                            )
                        }
                    }

                    override fun onFailure(call: Call<AtividadesListResponse>, t: Throwable) {
                        Log.e("AtividadeViewModel", "❌ Fallback falhou na conexão", t)
                        _atividadesState.value = AtividadesState.Error(
                            "Erro de conexão. Verifique sua internet."
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("AtividadeViewModel", "❌ Exceção no fallback", e)
                _atividadesState.value = AtividadesState.Error("Erro inesperado: ${e.message}")
            }
        }
    }
}

/**
 * Estados possíveis para a lista de atividades
 */
sealed class AtividadesState {
    object Loading : AtividadesState()
    data class Success(val atividades: List<AtividadeResponse>) : AtividadesState()
    data class Error(val message: String) : AtividadesState()
}

/**
 * Estados possíveis para detalhes de uma atividade
 */
sealed class AtividadeDetalheState {
    object Idle : AtividadeDetalheState()
    object Loading : AtividadeDetalheState()
    data class Success(val atividade: AtividadeResponse) : AtividadeDetalheState()
    data class Error(val message: String) : AtividadeDetalheState()
}

