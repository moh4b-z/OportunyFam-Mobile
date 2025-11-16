package com.oportunyfam_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.Categoria
import com.oportunyfam_mobile.model.CategoriasResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel para gerenciar categorias de atividades
 */
class CategoriaViewModel : ViewModel() {

    private val categoriaService = RetrofitFactory().getCategoriaService()

    // Estado das categorias
    private val _categoriasState = MutableStateFlow<CategoriasState>(CategoriasState.Loading)
    val categoriasState: StateFlow<CategoriasState> = _categoriasState.asStateFlow()

    // Cache de categorias (evita múltiplas chamadas)
    private var categoriasCache: List<Categoria>? = null


    init {
        // Carregar categorias ao inicializar
        buscarCategorias()
    }

    /**
     * Buscar todas as categorias da API
     */
    fun buscarCategorias(forceRefresh: Boolean = false) {
        // Se já temos cache e não é refresh forçado, usar cache
        if (categoriasCache != null && !forceRefresh) {
            _categoriasState.value = CategoriasState.Success(categoriasCache!!)
            Log.d("CategoriaViewModel", "✅ Usando cache de categorias (${categoriasCache!!.size} categorias)")
            return
        }

        _categoriasState.value = CategoriasState.Loading
        Log.d("CategoriaViewModel", "🔄 Buscando categorias da API...")
        Log.d("CategoriaViewModel", "📡 URL: https://oportunyfam-back-end.onrender.com/v1/oportunyfam/categorias")

        viewModelScope.launch {
            try {
                categoriaService.buscarTodasCategorias().enqueue(object : Callback<CategoriasResponse> {
                    override fun onResponse(
                        call: Call<CategoriasResponse>,
                        response: Response<CategoriasResponse>
                    ) {
                        Log.d("CategoriaViewModel", "📡 Resposta recebida - Código: ${response.code()}")

                        when {
                            response.isSuccessful && response.body() != null -> {
                                val body = response.body()!!
                                val categorias = body.categorias ?: emptyList()

                                Log.d("CategoriaViewModel", "📦 Response completa:")
                                Log.d("CategoriaViewModel", "   - status: ${body.status}")
                                Log.d("CategoriaViewModel", "   - status_code: ${body.status_code}")
                                Log.d("CategoriaViewModel", "   - messagem: ${body.messagem}")
                                Log.d("CategoriaViewModel", "   - categorias.size: ${categorias.size}")

                                if (categorias.isEmpty()) {
                                    Log.e("CategoriaViewModel", "❌ API retornou lista vazia")
                                    _categoriasState.value = CategoriasState.Error(
                                        "Nenhuma categoria disponível no momento"
                                    )
                                } else {
                                    Log.d("CategoriaViewModel", "✅ ${categorias.size} categorias carregadas da API:")
                                    categorias.forEach {
                                        Log.d("CategoriaViewModel", "   - ID ${it.id}: ${it.nome}")
                                    }

                                    // Salvar no cache
                                    categoriasCache = categorias
                                    _categoriasState.value = CategoriasState.Success(categorias)
                                }
                            }
                            response.code() == 404 -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("CategoriaViewModel", "❌ Endpoint de categorias não encontrado (404)")
                                Log.e("CategoriaViewModel", "Error body: $errorBody")
                                _categoriasState.value = CategoriasState.Error(
                                    "Endpoint de categorias não encontrado. Verifique a API."
                                )
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("CategoriaViewModel", "❌ Erro ${response.code()}: $errorBody")
                                _categoriasState.value = CategoriasState.Error(
                                    "Erro ao carregar categorias (${response.code()})"
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<CategoriasResponse>, t: Throwable) {
                        Log.e("CategoriaViewModel", "❌ Falha ao buscar categorias", t)
                        _categoriasState.value = CategoriasState.Error(
                            "Erro de conexão: ${t.message}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("CategoriaViewModel", "❌ Exceção ao buscar categorias", e)
                _categoriasState.value = CategoriasState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Limpar cache de categorias
     */
    fun limparCache() {
        categoriasCache = null
        Log.d("CategoriaViewModel", "🗑️ Cache de categorias limpo")
    }
}

/**
 * Estados possíveis para as categorias
 */
sealed class CategoriasState {
    object Loading : CategoriasState()
    data class Success(val categorias: List<Categoria>) : CategoriasState()
    data class Error(val message: String) : CategoriasState()
}

