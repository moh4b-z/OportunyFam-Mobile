package com.oportunyfam_mobile.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.Publicacao
import com.oportunyfam_mobile.model.PublicacoesListResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel para visualização de publicações
 * Nota: Este é o app para Responsáveis e Crianças (USUÁRIOS FINAIS)
 * Eles podem apenas VISUALIZAR publicações, não criar ou deletar
 * (Criar/deletar são funcionalidades exclusivas do app institucional)
 */
class PublicacaoViewModel : ViewModel() {

    private val publicacaoService = RetrofitFactory().getPublicacaoService()

    private val _publicacoesState = MutableStateFlow<PublicacoesState>(PublicacoesState.Loading)
    val publicacoesState: StateFlow<PublicacoesState> = _publicacoesState.asStateFlow()

    /**
     * Busca publicações de uma instituição específica
     * Usado quando o usuário visualiza o perfil de uma instituição
     */
    fun buscarPublicacoesPorInstituicao(instituicaoId: Int) {
        _publicacoesState.value = PublicacoesState.Loading
        Log.d("PublicacaoViewModel", "📱 Buscando publicações da instituição ID: $instituicaoId")

        publicacaoService.buscarPublicacoesPorInstituicao(instituicaoId).enqueue(object : Callback<PublicacoesListResponse> {
            override fun onResponse(call: Call<PublicacoesListResponse>, response: Response<PublicacoesListResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()
                    val publicacoes: List<Publicacao> = body?.publicacoes ?: emptyList()
                    Log.d("PublicacaoViewModel", "✅ ${publicacoes.size} publicações carregadas")
                    _publicacoesState.value = PublicacoesState.Success(publicacoes)
                } else {
                    Log.e("PublicacaoViewModel", "❌ Erro ${response.code()}")
                    _publicacoesState.value = PublicacoesState.Error("Erro ao carregar publicações")
                }
            }

            override fun onFailure(call: Call<PublicacoesListResponse>, t: Throwable) {
                Log.e("PublicacaoViewModel", "❌ Falha na conexão", t)
                _publicacoesState.value = PublicacoesState.Error("Erro de conexão")
            }
        })
    }

    /**
     * Busca todas as publicações (feed geral)
     * Usado na tela inicial para mostrar publicações de todas as instituições
     */
    fun buscarTodasPublicacoes() {
        _publicacoesState.value = PublicacoesState.Loading
        Log.d("PublicacaoViewModel", "📱 Buscando todas as publicações (feed)")

        publicacaoService.buscarTodasPublicacoes().enqueue(object : Callback<PublicacoesListResponse> {
            override fun onResponse(call: Call<PublicacoesListResponse>, response: Response<PublicacoesListResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()
                    val publicacoes: List<Publicacao> = body?.publicacoes ?: emptyList()
                    Log.d("PublicacaoViewModel", "✅ ${publicacoes.size} publicações carregadas no feed")
                    _publicacoesState.value = PublicacoesState.Success(publicacoes)
                } else {
                    Log.w("PublicacaoViewModel", "⚠️ Nenhuma publicação encontrada")
                    _publicacoesState.value = PublicacoesState.Success(emptyList())
                }
            }

            override fun onFailure(call: Call<PublicacoesListResponse>, t: Throwable) {
                Log.e("PublicacaoViewModel", "❌ Erro ao carregar feed", t)
                _publicacoesState.value = PublicacoesState.Error("Erro de conexão")
            }
        })
    }

    /**
     * Recarrega a lista atual
     */
    fun recarregar() {
        buscarTodasPublicacoes()
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


