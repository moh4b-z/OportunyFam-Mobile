package com.oportunyfam_mobile.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile.Service.FirebaseMensagemService
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.Mensagem
import com.oportunyfam_mobile.model.MensagemRequest
import com.oportunyfam_mobile.data.AuthDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ConversaUI(
    val id: Int,
    val nome: String,
    val ultimaMensagem: String,
    val hora: String,
    val imagem: Int,
    val online: Boolean = false,
    val mensagensNaoLidas: Int = 0,
    val pessoaId: Int
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val conversaService = RetrofitFactory().getConversaService()
    private val mensagemService = RetrofitFactory().getMensagemService()
    private val firebaseMensagemService: FirebaseMensagemService = FirebaseMensagemService()
    private val authDataStore = AuthDataStore(application)

    private val _conversas = MutableStateFlow<List<ConversaUI>>(emptyList())
    val conversas: StateFlow<List<ConversaUI>> = _conversas.asStateFlow()

    private val _mensagens = MutableStateFlow<List<Mensagem>>(emptyList())
    val mensagens: StateFlow<List<Mensagem>> = _mensagens.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _pessoaId = MutableStateFlow<Int?>(null)
    val pessoaId: StateFlow<Int?> = _pessoaId.asStateFlow()

    private var conversasCarregadas = false

    init {
        viewModelScope.launch {
            val authUser = authDataStore.loadAuthUser()

            // Extrair pessoa_id baseado no tipo de usuário
            _pessoaId.value = when (authUser?.type) {
                com.oportunyfam_mobile.data.AuthType.USUARIO -> {
                    (authUser.user as? com.oportunyfam_mobile.model.Usuario)?.pessoa_id
                }
                com.oportunyfam_mobile.data.AuthType.CRIANCA -> {
                    (authUser.user as? com.oportunyfam_mobile.model.Crianca)?.pessoa_id
                }
                else -> null
            }

            val nome = when (authUser?.type) {
                com.oportunyfam_mobile.data.AuthType.USUARIO -> {
                    (authUser.user as? com.oportunyfam_mobile.model.Usuario)?.nome
                }
                com.oportunyfam_mobile.data.AuthType.CRIANCA -> {
                    (authUser.user as? com.oportunyfam_mobile.model.Crianca)?.nome
                }
                else -> null
            }

            Log.d("ChatViewModel", "Pessoa logada: ID=${_pessoaId.value}, Nome=$nome, Tipo=${authUser?.type}")
        }
    }

    fun carregarConversas(forcarRecarregar: Boolean = false) {
        if (conversasCarregadas && !forcarRecarregar) {
            Log.d("ChatViewModel", "Conversas já carregadas")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pessoaId = _pessoaId.value
                if (pessoaId == null) {
                    _errorMessage.value = "Usuário não está logado"
                    _isLoading.value = false
                    return@launch
                }

                Log.d("ChatViewModel", "Carregando conversas para pessoa ID=$pessoaId")

                val response = withContext(Dispatchers.IO) {
                    conversaService.buscarPorIdPessoa(pessoaId)
                }

                if (response.isSuccessful) {
                    // ✅ Usa o helper getConversasList() que lida com "conversa" ou "conversas"
                    val conversasData = response.body()?.getConversasList() ?: emptyList()

                    _conversas.value = conversasData.map { conversa ->
                        ConversaUI(
                            id = conversa.id_conversa,
                            nome = conversa.outro_participante.nome,
                            ultimaMensagem = conversa.ultima_mensagem?.descricao ?: "Sem mensagens",
                            hora = formatarHora(conversa.ultima_mensagem?.data_envio),
                            imagem = com.oportunyfam_mobile.R.drawable.user,
                            online = false,
                            mensagensNaoLidas = 0,
                            pessoaId = conversa.outro_participante.id
                        )
                    }

                    conversasCarregadas = true
                    _errorMessage.value = null // ✅ Limpa erro após sucesso
                    Log.d("ChatViewModel", "Conversas carregadas: ${_conversas.value.size}")
                } else {
                    _errorMessage.value = "Erro ao carregar conversas"
                    Log.e("ChatViewModel", "Erro HTTP: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Sem conexão"
                Log.e("ChatViewModel", "Erro ao conectar: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun iniciarEscutaMensagens(conversaId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1️⃣ Carrega mensagens iniciais do backend
                val response = withContext(Dispatchers.IO) {
                    mensagemService.listarPorConversa(conversaId)
                }

                if (response.isSuccessful) {
                    val mensagensBackend = response.body()?.mensagens ?: emptyList()
                    _mensagens.value = mensagensBackend
                    Log.d("ChatViewModel", "✅ Mensagens carregadas do backend: ${mensagensBackend.size}")

                    // 2️⃣ Sincroniza mensagens do backend para o Firebase
                    launch(Dispatchers.IO) {
                        val result = firebaseMensagemService.sincronizarMensagens(conversaId, mensagensBackend)
                        if (result.isSuccess) {
                            Log.d("ChatViewModel", "✅ Mensagens sincronizadas com Firebase")
                        } else {
                            Log.e("ChatViewModel", "❌ Erro ao sincronizar com Firebase: ${result.exceptionOrNull()?.message}")
                        }
                    }
                } else {
                    _errorMessage.value = "Erro ao carregar mensagens"
                    Log.e("ChatViewModel", "❌ Erro HTTP: ${response.code()}")
                }

                _isLoading.value = false

                // 3️⃣ Inicia escuta em tempo real do Firebase (SEMPRE ativa)
                firebaseMensagemService.observarMensagens(conversaId).collect { mensagensFirebase ->
                    Log.d("ChatViewModel", "🔥 Firebase atualizou: ${mensagensFirebase.size} mensagens")

                    // ✅ CORRIGIDO: Atualiza com TODAS as mensagens do Firebase
                    // Isso garante que mudanças em tempo real sejam refletidas
                    if (mensagensFirebase.isNotEmpty()) {
                        _mensagens.value = mensagensFirebase.sortedBy { it.criado_em }
                        Log.d("ChatViewModel", "✅ UI atualizada com ${mensagensFirebase.size} mensagens")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar mensagens: ${e.message}"
                _isLoading.value = false
                Log.e("ChatViewModel", "❌ Erro ao iniciar escuta: ${e.message}", e)
            }
        }
    }

    fun enviarMensagem(conversaId: Int, pessoaId: Int, texto: String) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "📤 Enviando mensagem: conversa=$conversaId, pessoa=$pessoaId")

                val request = MensagemRequest(
                    id_conversa = conversaId,
                    id_pessoa = pessoaId,
                    descricao = texto
                )

                val response = withContext(Dispatchers.IO) {
                    mensagemService.criar(request)
                }

                if (response.isSuccessful) {
                    val mensagemCriada = response.body()?.mensagem

                    if (mensagemCriada != null) {
                        Log.d("ChatViewModel", "✅ Mensagem criada no backend: ID=${mensagemCriada.id}")

                        // Envia para o Firebase
                        val resultFirebase = withContext(Dispatchers.IO) {
                            firebaseMensagemService.enviarMensagem(mensagemCriada)
                        }

                        if (resultFirebase.isSuccess) {
                            Log.d("ChatViewModel", "✅ Mensagem enviada para Firebase: ${mensagemCriada.id}")
                        } else {
                            Log.e("ChatViewModel", "❌ Erro ao enviar para Firebase: ${resultFirebase.exceptionOrNull()?.message}")
                            _errorMessage.value = "Mensagem salva, mas erro na sincronização em tempo real"
                        }
                    } else {
                        Log.e("ChatViewModel", "❌ Resposta do backend sem mensagem")
                        _errorMessage.value = "Erro ao processar resposta do servidor"
                    }
                } else {
                    Log.e("ChatViewModel", "❌ Erro HTTP ao enviar mensagem: ${response.code()} - ${response.message()}")
                    _errorMessage.value = "Erro ao enviar mensagem"
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "❌ Exceção ao enviar mensagem", e)
                _errorMessage.value = "Erro ao enviar: ${e.message}"
            }
        }
    }

    fun limparErro() {
        _errorMessage.value = null
    }

    private fun formatarHora(dataHora: String?): String {
        if (dataHora == null) return "Agora"
        return try {
            val partes = dataHora.split(" ")
            if (partes.size > 1) {
                partes[1].substring(0, 5)
            } else {
                "Agora"
            }
        } catch (_: Exception) {
            "Agora"
        }
    }
}

