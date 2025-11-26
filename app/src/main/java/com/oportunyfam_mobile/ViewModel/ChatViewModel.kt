package com.oportunyfam_mobile.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile.Service.FirebaseMensagemService
import com.oportunyfam_mobile.Service.MensagemService
import com.oportunyfam_mobile.Service.ConversaService
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.Service.AzureBlobRetrofit
import com.oportunyfam_mobile.Config.AzureConfig
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.data.AuthType
import com.oportunyfam_mobile.model.Mensagem
import com.oportunyfam_mobile.model.MensagemRequest
import com.oportunyfam_mobile.model.Usuario
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.ConversaRequest
import com.oportunyfam_mobile.Utils.AudioRecorder
import com.oportunyfam_mobile.Utils.AudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class ConversaUI(
    val id: Int,
    val nome: String,
    val ultimaMensagem: String,
    val hora: String,
    val imagem: Int,
    val online: Boolean,
    val mensagensNaoLidas: Int = 0,
    val pessoaId: Int
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val conversaService: ConversaService = RetrofitFactory().getConversaService()
    private val mensagemService: MensagemService = RetrofitFactory().getMensagemService()
    private val firebaseMensagemService: FirebaseMensagemService = FirebaseMensagemService()
    private val authDataStore = AuthDataStore(application)

    // Audio recording and playing
    private val audioRecorder = AudioRecorder(application)
    private val audioPlayer = AudioPlayer(application)

    private val _conversas = MutableStateFlow<List<ConversaUI>>(emptyList())
    val conversas: StateFlow<List<ConversaUI>> = _conversas.asStateFlow()

    private val _mensagens = MutableStateFlow<List<Mensagem>>(emptyList())
    val mensagens: StateFlow<List<Mensagem>> = _mensagens.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Audio recording states
    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()

    private val _isUploadingAudio = MutableStateFlow(false)
    val isUploadingAudio: StateFlow<Boolean> = _isUploadingAudio.asStateFlow()

    // Audio player states
    private val _currentPlayingAudioUrl = MutableStateFlow<String?>(null)
    val currentPlayingAudioUrl: StateFlow<String?> = _currentPlayingAudioUrl.asStateFlow()

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    private val _audioProgress = MutableStateFlow<Pair<Int, Int>>(0 to 0) // (current, total) em ms
    val audioProgress: StateFlow<Pair<Int, Int>> = _audioProgress.asStateFlow()

    private var progressUpdateJob: kotlinx.coroutines.Job? = null

    // ID da pessoa logada (usuário ou criança)
    private val _pessoaId = MutableStateFlow<Int?>(null)
    val pessoaId: StateFlow<Int?> = _pessoaId.asStateFlow()

    // Tipo de usuário logado (USUARIO ou CRIANCA)
    private val _userType = MutableStateFlow<AuthType?>(null)
    val userType: StateFlow<AuthType?> = _userType.asStateFlow()

    // Flag para controlar se já carregou conversas
    private var conversasCarregadas = false

    init {
        // Carrega o ID da pessoa ao inicializar o ViewModel
        viewModelScope.launch {
            val authUser = authDataStore.loadAuthUser()

            if (authUser != null) {
                when (authUser.type) {
                    AuthType.USUARIO -> {
                        val usuario = authUser.user as Usuario
                        _pessoaId.value = usuario.pessoa_id
                        _userType.value = AuthType.USUARIO
                        Log.d("ChatViewModel", "Usuário logado: ID=${usuario.pessoa_id}, Nome=${usuario.nome}")
                    }
                    AuthType.CRIANCA -> {
                        val crianca = authUser.user as Crianca
                        _pessoaId.value = crianca.pessoa_id
                        _userType.value = AuthType.CRIANCA
                        Log.d("ChatViewModel", "Criança logada: ID=${crianca.pessoa_id}, Nome=${crianca.nome}")
                    }
                }
            } else {
                Log.e("ChatViewModel", "❌ Nenhum usuário logado encontrado")
            }
        }
    }

    /**
     * Carrega conversas da API usando o endpoint /conversas/pessoa/:id
     * Com cache offline: primeiro carrega do cache, depois atualiza da API
     * Se falhar (sem internet), mantém o cache
     */
    fun carregarConversas(forcarRecarregar: Boolean = false) {
        if (conversasCarregadas && !forcarRecarregar) {
            Log.d("ChatViewModel", "Conversas já carregadas, pulando requisição")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // Limpa erro anterior

            try {
                // ✅ Aguarda a pessoa estar carregada (pode estar sendo carregado do DataStore)
                var pessoaId = _pessoaId.value

                // Se ainda não carregou, aguarda um pouco (máximo 2 segundos)
                var tentativas = 0
                while (pessoaId == null && tentativas < 20) {
                    kotlinx.coroutines.delay(100) // Aguarda 100ms
                    pessoaId = _pessoaId.value
                    tentativas++
                }

                if (pessoaId == null) {
                    _errorMessage.value = "Erro ao carregar dados do usuário. Faça login novamente."
                    Log.e("ChatViewModel", "❌ Pessoa não carregada após aguardar")
                    _isLoading.value = false
                    return@launch
                }

                Log.d("ChatViewModel", "🔄 Carregando conversas da API para pessoa ID=$pessoaId")

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    conversaService.buscarPorIdPessoa(pessoaId)
                }

                if (response.isSuccessful) {
                    val conversasInstituicao = response.body()?.getConversasList() ?: emptyList()

                    Log.d("ChatViewModel", "📋 API retornou ${conversasInstituicao.size} conversas")

                    // Log detalhado de cada conversa
                    conversasInstituicao.forEachIndexed { index, conversa ->
                        Log.d("ChatViewModel", "  [$index] ID conversa=${conversa.id_conversa}, " +
                                "Pessoa=${conversa.outro_participante.nome} (ID=${conversa.outro_participante.id}), " +
                                "Última msg: ${conversa.ultima_mensagem?.descricao ?: "sem mensagens"}")
                    }

                    // ✅ FILTRAR: Mostrar TODAS as conversas únicas (diferentes pessoas OU diferentes IDs de conversa)
                    // Se duas pessoas têm o mesmo nome, mantém ambas as conversas mais recentes
                    val conversasUnicas = conversasInstituicao
                        .groupBy { it.outro_participante.id } // Agrupa pelo ID da pessoa
                        .flatMap { (pessoaId, conversasGrupo) ->
                            Log.d("ChatViewModel", "  📂 Grupo pessoa_id=$pessoaId: ${conversasGrupo.size} conversa(s)")

                            // Se tem mais de uma conversa com a mesma pessoa, pega só a mais recente
                            if (conversasGrupo.size > 1) {
                                val maisRecente = conversasGrupo.maxByOrNull { conversa ->
                                    conversa.ultima_mensagem?.data_envio ?: "1970-01-01 00:00:00"
                                }
                                Log.d("ChatViewModel", "    ⚠️ ${conversasGrupo.size} duplicatas! Mantendo apenas ID=${maisRecente?.id_conversa}")
                                listOfNotNull(maisRecente)
                            } else {
                                conversasGrupo
                            }
                        }

                    Log.d("ChatViewModel", "🔹 Após remover duplicatas: ${conversasUnicas.size} conversas únicas")
                    conversasUnicas.forEach { conversa ->
                        Log.d("ChatViewModel", "  ✅ ID=${conversa.id_conversa}, ${conversa.outro_participante.nome} (pessoa_id=${conversa.outro_participante.id})")
                    }

                    // Atualiza as conversas na UI (apenas únicas)
                    _conversas.value = conversasUnicas.map { conversa ->
                        ConversaUI(
                            id = conversa.id_conversa,
                            nome = conversa.outro_participante.nome,
                            ultimaMensagem = conversa.ultima_mensagem?.descricao ?: "Sem mensagens",
                            hora = formatarHora(conversa.ultima_mensagem?.data_envio),
                            imagem = com.oportunyfam_mobile.R.drawable.perfil,
                            online = false,
                            mensagensNaoLidas = 0,
                            pessoaId = conversa.outro_participante.id
                        )
                    }

                    conversasCarregadas = true
                    Log.d("ChatViewModel", "✅ Conversas carregadas e filtradas: ${_conversas.value.size}")
                } else {
                    _errorMessage.value = "Erro ao carregar conversas: ${response.message()}"
                    Log.e("ChatViewModel", "❌ Erro API: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao conectar: ${e.message}"
                Log.e("ChatViewModel", "❌ Erro ao conectar: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private var firebaseJob: kotlinx.coroutines.Job? = null
    private var firebaseConversaObservadaId: Int? = null

    fun iniciarEscutaMensagens(conversaId: Int) {
        // evita abrir múltiplas escutas para mesma conversa
        if (firebaseConversaObservadaId == conversaId && firebaseJob?.isActive == true) return

        // cancela qualquer escuta anterior
        pararEscutaMensagens()
        firebaseConversaObservadaId = conversaId

        firebaseJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1) Carrega do backend (fonte da verdade)
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    mensagemService.listarPorConversa(conversaId)
                }

                if (response.isSuccessful) {
                    val mensagensBackend = response.body()?.mensagens ?: emptyList()
                    _mensagens.value = mensagensBackend.sortedBy { it.criado_em }
                    // Sincroniza com firebase (sem apagar) em background
                    launch(kotlinx.coroutines.Dispatchers.IO) {
                        firebaseMensagemService.sincronizarMensagens(conversaId, mensagensBackend)
                    }
                }

                _isLoading.value = false

                // 2) Escuta eventos incrementais do Firebase
                firebaseMensagemService.observarMensagensEventos(conversaId).collect { event ->
                    when (event.type) {
                        "added" -> addOrUpdateMensagem(event.mensagem)
                        "changed" -> addOrUpdateMensagem(event.mensagem)
                        "removed" -> removeMensagem(event.mensagem.id)
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar mensagens: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun pararEscutaMensagens() {
        firebaseJob?.cancel()
        firebaseJob = null
        firebaseConversaObservadaId = null
    }

    private fun addOrUpdateMensagem(m: Mensagem) {
        // mantém unicidade por ID e ordena por criado_em
        Log.d("ChatViewModel", "📨 Mensagem recebida: ID=${m.id}, tipo=${m.tipo}, audio_url=${m.audio_url}, descricao=${m.descricao}")
        val map = _mensagens.value.associateBy { it.id }.toMutableMap()
        map[m.id] = m
        _mensagens.value = map.values.sortedBy { it.criado_em }
    }

    private fun removeMensagem(mensagemId: Int) {
        _mensagens.value = _mensagens.value.filterNot { it.id == mensagemId }
    }


    /**
     * Recarrega mensagens da API (útil para refresh manual)
     */
    fun recarregarMensagens(conversaId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "🔄 Recarregando mensagens da API...")

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    mensagemService.listarPorConversa(conversaId)
                }

                if (response.isSuccessful) {
                    val mensagensBackend = response.body()?.mensagens ?: emptyList()
                    _mensagens.value = mensagensBackend
                    Log.d("ChatViewModel", "✅ ${mensagensBackend.size} mensagens recarregadas")
                } else if (response.code() == 404) {
                    _mensagens.value = emptyList()
                    Log.d("ChatViewModel", "✅ Nenhuma mensagem na conversa")
                } else {
                    Log.e("ChatViewModel", "❌ Erro ao recarregar: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "❌ Erro ao recarregar mensagens", e)
            }
        }
    }


    fun enviarMensagem(conversaId: Int, pessoaId: Int, texto: String) {
        viewModelScope.launch {
            try {
                // 1. Envia para o backend (fonte da verdade)
                val request = MensagemRequest(
                    id_conversa = conversaId,
                    id_pessoa = pessoaId,
                    descricao = texto
                )
                val response = mensagemService.criar(request)

                if (response.isSuccessful) {
                    val mensagemCriada = response.body()?.mensagem

                    if (mensagemCriada != null) {
                        // 2. Adiciona mensagem localmente para aparecer imediatamente
                        addOrUpdateMensagem(mensagemCriada)
                        Log.d("ChatViewModel", "✅ Mensagem enviada e adicionada localmente: ${mensagemCriada.id}")

                        // 3. Envia para o Firebase (notifica em tempo real)
                        launch(kotlinx.coroutines.Dispatchers.IO) {
                            val result = firebaseMensagemService.enviarMensagem(mensagemCriada)
                            if (result.isFailure) {
                                Log.e("ChatViewModel", "⚠️ Falha ao notificar Firebase: ${result.exceptionOrNull()}")
                                // Não mostra erro para o usuário, apenas loga
                            }
                        }

                        // 4. Atualiza a lista de conversas em background
                        launch {
                            carregarConversas(forcarRecarregar = true)
                        }
                    }
                } else {
                    _errorMessage.value = "Erro ao enviar mensagem"
                    Log.e("ChatViewModel", "Erro: ${response.errorBody()?.string()}")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // ✅ Coroutine cancelada - NÃO é erro
                Log.d("ChatViewModel", "⏹️ Envio de mensagem cancelado")
                throw e // Re-throw para propagar o cancelamento
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao enviar: ${e.message}"
                Log.e("ChatViewModel", "Erro ao enviar mensagem", e)
            }
        }
    }

    fun limparErro() {
        _errorMessage.value = null
    }

    /**
     * Inicia a gravação de áudio
     */
    fun startAudioRecording() {
        viewModelScope.launch {
            try {
                val file = audioRecorder.startRecording()
                if (file != null) {
                    _isRecordingAudio.value = true
                    _recordingDuration.value = 0

                    // Atualiza duração a cada segundo
                    launch {
                        while (_isRecordingAudio.value) {
                            kotlinx.coroutines.delay(1000)
                            _recordingDuration.value = audioRecorder.getCurrentDuration()
                        }
                    }

                    Log.d("ChatViewModel", "🎤 Gravação de áudio iniciada")
                } else {
                    _errorMessage.value = "Erro ao iniciar gravação de áudio"
                    Log.e("ChatViewModel", "❌ Falha ao iniciar gravação")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao gravar: ${e.message}"
                Log.e("ChatViewModel", "Erro ao iniciar gravação", e)
            }
        }
    }

    /**
     * Para a gravação e envia o áudio
     */
    fun stopAudioRecordingAndSend(conversaId: Int, pessoaId: Int) {
        viewModelScope.launch {
            try {
                _isRecordingAudio.value = false
                val (audioFile, duration) = audioRecorder.stopRecording()

                if (audioFile != null && audioFile.exists()) {
                    Log.d("ChatViewModel", "🎤 Áudio gravado: ${audioFile.absolutePath}, duração: $duration segundos")

                    // Verifica se a duração é maior que 1 segundo
                    if (duration < 1) {
                        _errorMessage.value = "Áudio muito curto"
                        audioFile.delete()
                        return@launch
                    }

                    enviarMensagemAudio(conversaId, pessoaId, audioFile, duration)
                } else {
                    _errorMessage.value = "Erro ao salvar áudio"
                    Log.e("ChatViewModel", "❌ Arquivo de áudio não encontrado")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao parar gravação: ${e.message}"
                Log.e("ChatViewModel", "Erro ao parar gravação", e)
            }
        }
    }

    /**
     * Cancela a gravação de áudio
     */
    fun cancelAudioRecording() {
        _isRecordingAudio.value = false
        _recordingDuration.value = 0
        audioRecorder.cancelRecording()
        Log.d("ChatViewModel", "🎤 Gravação de áudio cancelada")
    }

    /**
     * Envia mensagem de áudio
     */
    private fun enviarMensagemAudio(conversaId: Int, pessoaId: Int, audioFile: File, duration: Int) {
        viewModelScope.launch {
            _isUploadingAudio.value = true

            try {
                // 1. Upload do áudio para Azure
                Log.d("ChatViewModel", "☁️ Fazendo upload do áudio para Azure...")
                val audioUrl = AzureBlobRetrofit.uploadAudioToAzure(
                    audioFile = audioFile,
                    storageAccount = AzureConfig.STORAGE_ACCOUNT,
                    sasToken = AzureConfig.SAS_TOKEN,
                    containerName = AzureConfig.CONTAINER_NAME
                )

                if (audioUrl == null) {
                    _errorMessage.value = "Erro ao fazer upload do áudio"
                    Log.e("ChatViewModel", "❌ Falha no upload para Azure")
                    _isUploadingAudio.value = false
                    audioFile.delete()
                    return@launch
                }

                Log.d("ChatViewModel", "✅ Upload concluído: $audioUrl")

                // 2. Criar mensagem no backend
                val request = MensagemRequest(
                    id_conversa = conversaId,
                    id_pessoa = pessoaId,
                    descricao = "Áudio ($duration s)",
                    tipo = "AUDIO",
                    audio_url = audioUrl,
                    audio_duracao = duration
                )

                val response = mensagemService.criar(request)

                if (response.isSuccessful) {
                    val mensagemCriada = response.body()?.mensagem

                    if (mensagemCriada != null) {
                        // 3. Adiciona mensagem localmente
                        addOrUpdateMensagem(mensagemCriada)
                        Log.d("ChatViewModel", "✅ Mensagem de áudio enviada: ${mensagemCriada.id}")

                        // 4. Envia para o Firebase
                        launch(kotlinx.coroutines.Dispatchers.IO) {
                            val result = firebaseMensagemService.enviarMensagem(mensagemCriada)
                            if (result.isFailure) {
                                Log.e("ChatViewModel", "⚠️ Falha ao notificar Firebase: ${result.exceptionOrNull()}")
                            }
                        }

                        // 5. Atualiza lista de conversas
                        launch {
                            carregarConversas(forcarRecarregar = true)
                        }
                    }
                } else {
                    _errorMessage.value = "Erro ao enviar mensagem de áudio"
                    Log.e("ChatViewModel", "Erro: ${response.errorBody()?.string()}")
                }

                // Limpa o arquivo temporário
                audioFile.delete()

            } catch (e: kotlinx.coroutines.CancellationException) {
                Log.d("ChatViewModel", "⏹️ Envio de áudio cancelado")
                throw e
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao enviar áudio: ${e.message}"
                Log.e("ChatViewModel", "Erro ao enviar áudio", e)
                audioFile.delete()
            } finally {
                _isUploadingAudio.value = false
            }
        }
    }

    /**
     * Reproduz ou pausa um áudio
     */
    fun playAudio(audioUrl: String) {
        // Se já está com este áudio carregado
        if (_currentPlayingAudioUrl.value == audioUrl) {
            // Se está tocando, pausar
            if (audioPlayer.isPlaying()) {
                pauseAudio()
                return
            } else {
                // Se estava pausado, retomar
                audioPlayer.playAudio(audioUrl)
                _isAudioPlaying.value = true
                startProgressUpdateJob(audioUrl)
                Log.d("ChatViewModel", "▶️ Retomando áudio: $audioUrl")
                return
            }
        }

        // Para qualquer áudio anterior
        stopAudio()

        // Inicia novo áudio
        _currentPlayingAudioUrl.value = audioUrl
        _isAudioPlaying.value = true
        _audioProgress.value = 0 to 0

        audioPlayer.playAudio(
            audioUrl = audioUrl,
            onCompletion = {
                // Quando terminar, reseta estado
                progressUpdateJob?.cancel()
                _currentPlayingAudioUrl.value = null
                _isAudioPlaying.value = false
                _audioProgress.value = 0 to 0

                Log.d("ChatViewModel", "✅ Áudio terminado: $audioUrl")

                // Toca próximo áudio se houver
                playNextAudio(audioUrl)
            },
            onProgress = { current, total ->
                _audioProgress.value = current to total
            }
        )

        startProgressUpdateJob(audioUrl)
        Log.d("ChatViewModel", "▶️ Reproduzindo áudio: $audioUrl")
    }

    /**
     * Inicia job para atualizar progresso do áudio
     */
    private fun startProgressUpdateJob(audioUrl: String) {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (_currentPlayingAudioUrl.value == audioUrl && audioPlayer.isPlaying()) {
                val current = audioPlayer.getCurrentPosition()
                val total = audioPlayer.getDuration()
                if (total > 0) {
                    _audioProgress.value = current to total
                }
                kotlinx.coroutines.delay(100) // Atualiza a cada 100ms
            }
        }
    }

    /**
     * Pausa o áudio atual
     */
    fun pauseAudio() {
        audioPlayer.pauseAudio()
        _isAudioPlaying.value = false
        progressUpdateJob?.cancel()
        Log.d("ChatViewModel", "⏸️ Áudio pausado")
    }

    /**
     * Para a reprodução de áudio
     */
    fun stopAudio() {
        audioPlayer.stopAudio()
        _currentPlayingAudioUrl.value = null
        _isAudioPlaying.value = false
        _audioProgress.value = 0 to 0
        progressUpdateJob?.cancel()
    }

    /**
     * Move a posição de reprodução para um ponto específico
     */
    fun seekToPosition(audioUrl: String, positionMs: Int) {
        // Só permite buscar se for o áudio atual
        if (_currentPlayingAudioUrl.value == audioUrl) {
            audioPlayer.seekTo(positionMs)
            // Atualiza o progresso imediatamente
            val total = audioPlayer.getDuration()
            _audioProgress.value = positionMs to total
            Log.d("ChatViewModel", "⏩ Posição do áudio alterada para: ${positionMs}ms")
        }
    }

    /**
     * Toca o próximo áudio na lista (se houver)
     */
    private fun playNextAudio(currentAudioUrl: String) {
        val audioMessages = _mensagens.value.filter {
            it.tipo == com.oportunyfam_mobile.model.TipoMensagem.AUDIO &&
                    it.audio_url != null
        }.sortedBy { it.criado_em }

        val currentIndex = audioMessages.indexOfFirst { it.audio_url == currentAudioUrl }
        if (currentIndex >= 0 && currentIndex < audioMessages.size - 1) {
            val nextAudio = audioMessages[currentIndex + 1]
            nextAudio.audio_url?.let { url ->
                Log.d("ChatViewModel", "⏭️ Reproduzindo próximo áudio")
                playAudio(url)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        if (audioRecorder.isRecording()) {
            audioRecorder.cancelRecording()
        }
    }

    /**
     * Cria uma nova conversa com outra pessoa
     * Retorna o ID da conversa criada
     * @param outraPessoaId ID da pessoa com quem criar a conversa
     */
    suspend fun criarConversa(outraPessoaId: Int): Int? {
        return try {
            val pessoaId = _pessoaId.value
            if (pessoaId == null) {
                Log.e("ChatViewModel", "❌ Pessoa ID não encontrado")
                return null
            }

            // Verifica se já existe uma conversa com essa pessoa
            val conversaExistente = _conversas.value.find { it.pessoaId == outraPessoaId }
            if (conversaExistente != null) {
                Log.d("ChatViewModel", "✅ Conversa existente encontrada: ID=${conversaExistente.id}")
                return conversaExistente.id
            }

            // Se não existe, cria uma nova conversa
            Log.d("ChatViewModel", "🔄 Criando nova conversa com pessoa ID=$outraPessoaId")
            val request = ConversaRequest(participantes = listOf(pessoaId, outraPessoaId))

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                conversaService.criar(request)
            }

            if (response.isSuccessful) {
                val novaConversa = response.body()?.conversa
                if (novaConversa != null) {
                    Log.d("ChatViewModel", "✅ Nova conversa criada: ID=${novaConversa.id}")
                    // Recarrega a lista de conversas para incluir a nova
                    carregarConversas(forcarRecarregar = true)
                    return novaConversa.id
                }
            } else {
                Log.e("ChatViewModel", "❌ Erro ao criar conversa: ${response.errorBody()?.string()}")
                _errorMessage.value = "Erro ao criar conversa"
            }

            null
        } catch (e: Exception) {
            Log.e("ChatViewModel", "❌ Erro ao criar conversa", e)
            _errorMessage.value = "Erro ao conectar: ${e.message}"
            null
        }
    }

    private fun formatarHora(dataHora: String?): String {
        if (dataHora == null) return "Agora"
        return try {
            // Formato: "2025-11-06 12:49:09.000000"
            val partes = dataHora.split(" ")
            if (partes.size > 1) {
                val hora = partes[1].substring(0, 5)
                hora
            } else {
                "Agora"
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Erro ao formatar hora: $dataHora", e)
            "Agora"
        }
    }
}
