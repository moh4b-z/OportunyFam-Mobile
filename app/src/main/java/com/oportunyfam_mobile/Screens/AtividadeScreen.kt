package com.oportunyfam_mobile.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.MainActivity.NavRoutes
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.ViewModel.AtividadeViewModel
import com.oportunyfam_mobile.ViewModel.InscricaoViewModel
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.model.AtividadeResponse
import com.oportunyfam_mobile.model.CriancaMini
import com.oportunyfam_mobile.model.InscricaoRequest
import com.oportunyfam_mobile.model.MatriculaRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtividadeScreen(navController: NavHostController?, atividadeId: Int) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // List state to allow programmatic scrolling to show newly created inscrição
    val listState: LazyListState = rememberLazyListState()

    val atividadeViewModel: AtividadeViewModel = viewModel()
    val atividadeDetalheState by atividadeViewModel.atividadeDetalheState.collectAsState()

    val inscricaoViewModel: InscricaoViewModel = viewModel()
    val inscricoesState by inscricaoViewModel.inscricoesState.collectAsState()

    // Estados para inscrever
    var showInscreverDialog by remember { mutableStateOf(false) }
    var criancasList by remember { mutableStateOf<List<CriancaMini>>(emptyList()) }
    var isLoadingCriancas by remember { mutableStateOf(false) }
    var inscricaoMessage by remember { mutableStateOf<String?>(null) }
    var showParticiparDialog by remember { mutableStateOf(false) }
    var showChildSuggestDialog by remember { mutableStateOf(false) }
    var aulaToParticipateId by remember { mutableStateOf<Int?>(null) }
    // guarda o numero de aulas da atividade atual para calcular o índice do bloco 'Inscrições'
    var currentAulasCount by remember { mutableStateOf(0) }
    // tipo de auth carregado (USUARIO ou CRIANCA) — usado para ajustar mensagens de UI
    var currentAuthType by remember { mutableStateOf<com.oportunyfam_mobile.data.AuthType?>(null) }
    var currentChildId by remember { mutableStateOf<Int?>(null) }

    val authDataStore = remember { AuthDataStore(context) }

    // Função suspensa para carregar as crianças do usuário (executada em coroutine)
    suspend fun loadCriancasDoUsuario(): List<CriancaMini> {
        return withContext(Dispatchers.IO) {
            try {
                val auth = authDataStore.loadAuthUser()
                val usuarioId = if (auth?.type == com.oportunyfam_mobile.data.AuthType.USUARIO) {
                    (auth.user as? com.oportunyfam_mobile.model.Usuario)?.usuario_id
                } else null

                if (usuarioId == null) return@withContext emptyList<CriancaMini>()

                val resp = RetrofitFactory().getUsuarioService().buscarPorId(usuarioId).execute()
                if (resp.isSuccessful) {
                    val usuarioResp = resp.body()?.usuario
                    val miniList = usuarioResp?.criancas_dependentes ?: emptyList()
                    // Map to CriancaMini so we keep id_responsavel available
                    return@withContext miniList.map { mini ->
                        CriancaMini(
                            id_crianca = mini.id_crianca,
                            id_responsavel = mini.id_responsavel,
                            nome = mini.nome,
                            foto_perfil = mini.foto_perfil,
                            id_pessoa = mini.id_pessoa
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AtividadeScreen", "Erro ao carregar criancas: ${e.message}", e)
            }
            return@withContext emptyList<CriancaMini>()
        }
    }

    LaunchedEffect(atividadeId) {
        // load auth type once for dialog messaging
        try {
            val a = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }
            currentAuthType = a?.type
            // if logged as CRIANCA, try to extract crianca_id
            if (a?.type == com.oportunyfam_mobile.data.AuthType.CRIANCA) {
                val cri = when (a.user) {
                    is com.oportunyfam_mobile.model.Crianca -> a.user as com.oportunyfam_mobile.model.Crianca
                    else -> try {
                        com.google.gson.Gson().toJson(a.user).let { com.google.gson.Gson().fromJson(it, com.oportunyfam_mobile.model.Crianca::class.java) }
                    } catch (_: Exception) { null }
                }
                currentChildId = cri?.crianca_id
            }
            Log.d("AtividadeScreen", "LaunchedEffect loaded auth: type=${a?.type} user=${a?.user} childId=$currentChildId")
        } catch (ex: Exception) {
            currentAuthType = null
            currentChildId = null
            Log.e("AtividadeScreen", "Erro loading auth in LaunchedEffect: ${ex.message}")
        }

        atividadeViewModel.buscarAtividadePorId(atividadeId)
        inscricaoViewModel.buscarInscricoesPorAtividade(atividadeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Atividade") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = { BarraTarefas(navController = navController, currentRoute = NavRoutes.HOME) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (atividadeDetalheState) {
                is com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Idle -> {
                    /* aguardando ação */
                }
                is com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                is com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Error -> {
                    val msg = (atividadeDetalheState as com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Error).message
                    Text(text = msg, color = Color.Red, modifier = Modifier.align(Alignment.TopCenter))
                }
                is com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Success -> {
                    val atividade = (atividadeDetalheState as com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Success).atividade
                    AtividadeDetailContent(
                        atividade = atividade,
                        inscricoesState = inscricoesState,
                        onInscrever = {
                            // Ao clicar no botão de inscrever: carregar crianças e abrir diálogo
                            // captura o número de aulas para o scroll target
                            currentAulasCount = atividade.aulas.size
                            coroutineScope.launch {
                                isLoadingCriancas = true
                                val list = loadCriancasDoUsuario()
                                criancasList = list
                                isLoadingCriancas = false
                                showInscreverDialog = true
                            }
                        },
                        onParticipar = { aulaId ->
                            // If logged as CRIANCA, open special suggest dialog; otherwise open child selection dialog
                            if (currentAuthType == com.oportunyfam_mobile.data.AuthType.CRIANCA) {
                                aulaToParticipateId = aulaId
                                showChildSuggestDialog = true
                            } else {
                                coroutineScope.launch {
                                    aulaToParticipateId = aulaId
                                    isLoadingCriancas = true
                                    val list = loadCriancasDoUsuario()
                                    criancasList = list
                                    isLoadingCriancas = false
                                    showParticiparDialog = true
                                }
                            }
                        },
                        onSuggest = { aulaId ->
                            // existing suggestion flow uses atividade.atividade_id; leave as is
                            inscricaoMessage = null
                            coroutineScope.launch {
                                try {
                                    val auth = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }
                                    val criancaId = when (auth?.user) {
                                        is com.oportunyfam_mobile.model.Crianca -> (auth.user as com.oportunyfam_mobile.model.Crianca).crianca_id
                                        else -> try {
                                            com.google.gson.Gson().toJson(auth?.user).let { com.google.gson.Gson().fromJson(it, com.oportunyfam_mobile.model.Crianca::class.java) }?.crianca_id
                                        } catch (e: Exception) { null }
                                    }

                                    if (criancaId == null) {
                                        inscricaoMessage = "Não foi possível identificar a criança logada"
                                        return@launch
                                    }

                                    val req = InscricaoRequest(id_crianca = criancaId, id_atividade = atividade.atividade_id, id_responsavel = null)
                                    val resp = withContext(Dispatchers.IO) { RetrofitFactory().getInscricaoService().criarInscricao(req).execute() }
                                    if (resp.isSuccessful) {
                                        inscricaoMessage = "Sugestão de inscrição enviada ao responsável"
                                        inscricaoViewModel.buscarInscricoesPorAtividade(atividade.atividade_id)
                                    } else {
                                        inscricaoMessage = "Erro ao sugerir inscrição: ${resp.code()}"
                                    }
                                } catch (e: Exception) {
                                    inscricaoMessage = "Falha na conexão"
                                    Log.e("AtividadeScreen", "Erro sugerir inscrição: ${e.message}", e)
                                }
                            }
                        },
                        listState = listState,
                        currentAuthType = currentAuthType,
                        currentChildId = currentChildId
                    )
                }
            }
        }

        // Diálogo de seleção de criança e criação de inscrição
        if (showInscreverDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showInscreverDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .wrapContentHeight()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Header com cor do app
                        val headerBrush = Brush.horizontalGradient(listOf(Color(0xFFFFA000), Color(0xFFFFD27A)))
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(headerBrush), contentAlignment = Alignment.Center) {
                            Text(text = "Escolha a criança", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isLoadingCriancas) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else if (criancasList.isEmpty()) {
                            Text("Nenhuma criança disponível. Cadastre uma criança primeiro.", modifier = Modifier.padding(12.dp))
                        } else {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                criancasList.forEach { c ->
                                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                                        Row(modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            // avatar placeholder
                                            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFD27A)), contentAlignment = Alignment.Center) {
                                                Text(text = c.nome.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = c.nome,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Button(onClick = {
                                                // criar inscricao
                                                showInscreverDialog = false
                                                inscricaoMessage = null

                                                coroutineScope.launch {
                                                    val auth = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }
                                                    val idResponsavel = if (auth?.type == com.oportunyfam_mobile.data.AuthType.USUARIO) c.id_responsavel else null

                                                    Log.d("AtividadeScreen", "🔔 Criando inscrição -> crianca_id=${c.id_crianca}, atividadeId=$atividadeId, idResponsavel=$idResponsavel")

                                                    val request = InscricaoRequest(id_crianca = c.id_crianca, id_atividade = atividadeId, id_responsavel = idResponsavel)

                                                    RetrofitFactory().getInscricaoService().criarInscricao(request).enqueue(object: Callback<com.oportunyfam_mobile.model.InscricaoCriadaResponse> {
                                                        override fun onResponse(call: Call<com.oportunyfam_mobile.model.InscricaoCriadaResponse>, response: Response<com.oportunyfam_mobile.model.InscricaoCriadaResponse>) {
                                                            Log.d("AtividadeScreen", "Inscrição response code=${response.code()}")
                                                            if (response.isSuccessful) {
                                                                inscricaoMessage = "Inscrição solicitada com sucesso"
                                                                inscricaoViewModel.buscarInscricoesPorAtividade(atividadeId)
                                                                // scroll to the Inscrições block so user sees the new pending card
                                                                coroutineScope.launch {
                                                                    // target index: aulas.size + 3 (header,aulas header,aulas N,detalhes,inscricoes)
                                                                    val target = currentAulasCount + 3
                                                                    delay(300)
                                                                    try {
                                                                        listState.animateScrollToItem(target)
                                                                    } catch (_: Exception) { /* ignore */ }
                                                                }
                                                            } else {
                                                                inscricaoMessage = "Erro ao inscrever: ${response.code()}"
                                                                Log.e("AtividadeScreen", "Erro ao inscrever: ${response.errorBody()?.string()}")
                                                            }
                                                        }

                                                        override fun onFailure(call: Call<com.oportunyfam_mobile.model.InscricaoCriadaResponse>, t: Throwable) {
                                                            inscricaoMessage = "Falha na conexão"
                                                            Log.e("AtividadeScreen", "Falha criar inscricao", t)
                                                        }
                                                    })
                                                }

                                            }) {
                                                Text("Inscrever")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { showInscreverDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Fechar", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            }
        }

        // Diálogo para participar de uma aula (selecionar criança)
        if (showParticiparDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showParticiparDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .wrapContentHeight()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val headerBrush = Brush.horizontalGradient(listOf(Color(0xFFFFA000), Color(0xFFFFD27A)))
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(headerBrush), contentAlignment = Alignment.Center) {
                            Text(text = "Quem vai participar?", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isLoadingCriancas) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else if (criancasList.isEmpty()) {
                            Text("Nenhuma criança disponível. Cadastre uma criança primeiro.", modifier = Modifier.padding(12.dp))
                        } else {
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                criancasList.forEach { c ->
                                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))) {
                                        Row(modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFD27A)), contentAlignment = Alignment.Center) {
                                                Text(text = c.nome.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = c.nome, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Button(onClick = {
                                                // flow: find existing inscricao for this child & activity; if not found, create it; then create matricula
                                                showParticiparDialog = false
                                                inscricaoMessage = null
                                                coroutineScope.launch {
                                                    val aulaId = aulaToParticipateId ?: return@launch
                                                    var inscricaoId: Int? = null
                                                    // try to find in inscricoesState
                                                    if (inscricoesState is com.oportunyfam_mobile.ViewModel.InscricoesState.Success) {
                                                        val inscritos = (inscricoesState as com.oportunyfam_mobile.ViewModel.InscricoesState.Success).inscricoes
                                                        val found = inscritos.find { it.crianca_id == c.id_crianca }
                                                        if (found != null) {
                                                            inscricaoId = found.inscricao_id
                                                        }
                                                    }

                                                    // if not found, create inscrição synchronously
                                                    if (inscricaoId == null) {
                                                        try {
                                                            val auth = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }
                                                            val idResponsavel = if (auth?.type == com.oportunyfam_mobile.data.AuthType.USUARIO) c.id_responsavel else null
                                                            val req = InscricaoRequest(id_crianca = c.id_crianca, id_atividade = atividadeId, id_responsavel = idResponsavel)
                                                            val resp = withContext(Dispatchers.IO) { RetrofitFactory().getInscricaoService().criarInscricao(req).execute() }
                                                            if (resp.isSuccessful) {
                                                                // InscricaoCriadaResponse.inscricao geralmente tem .id (InscricaoSimples)
                                                                inscricaoId = resp.body()?.inscricao?.id
                                                            } else {
                                                                Log.e("AtividadeScreen", "Erro criando inscrição para matricula: ${resp.code()}")
                                                            }
                                                        } catch (e: Exception) {
                                                            Log.e("AtividadeScreen", "Erro criar inscricao: ${e.message}", e)
                                                        }
                                                    }

                                                    if (inscricaoId == null) {
                                                        inscricaoMessage = "Não foi possível obter/criar inscrição"
                                                        return@launch
                                                    }

                                                    // create Matricula
                                                    if (inscricaoId != null) {
                                                        val finalInscricaoId = inscricaoId
                                                        try {
                                                            val matriculaReq = MatriculaRequest(id_inscricao_atividade = finalInscricaoId, id_aula_atividade = aulaId, presente = false, nota_observacao = "")
                                                            val matResp = withContext(Dispatchers.IO) { RetrofitFactory().getMatriculaService().criarMatricula(matriculaReq).execute() }
                                                            if (matResp.isSuccessful) {
                                                                inscricaoMessage = "Participação registrada com sucesso"
                                                                // refresh data so UI reflects new matrícula/participação
                                                                try {
                                                                    atividadeViewModel.buscarAtividadePorId(atividadeId)
                                                                    inscricaoViewModel.buscarInscricoesPorAtividade(atividadeId)
                                                                } catch (_: Exception) { }
                                                                // clear selection
                                                                aulaToParticipateId = null
                                                            } else {
                                                                inscricaoMessage = "Erro ao registrar participação: ${matResp.code()}"
                                                                Log.e("AtividadeScreen", "Erro criar matricula: ${matResp.errorBody()?.string()}")
                                                            }
                                                        } catch (e: Exception) {
                                                            inscricaoMessage = "Falha na conexão"
                                                            Log.e("AtividadeScreen", "Falha criar matricula", e)
                                                        }
                                                    } else {
                                                        inscricaoMessage = "Não foi possível obter/criar inscrição"
                                                    }
                                                }
                                            }) {
                                                Text("Participar")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { showParticiparDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Fechar", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            }
        }

        // Diálogo especial para participação de CRIANCA (sugestão de inscrição)
        if (showChildSuggestDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showChildSuggestDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .wrapContentHeight()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val headerBrush = Brush.horizontalGradient(listOf(Color(0xFFFFA000), Color(0xFFFFD27A)))
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(headerBrush), contentAlignment = Alignment.Center) {
                            Text(text = "Sugestão de Participação", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Você está logado como uma criança. Sua participação nesta atividade será sugerida ao responsável.", modifier = Modifier.padding(12.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(onClick = {
                            // fluxo de sugestão de inscrição para CRIANCA
                            showChildSuggestDialog = false
                            inscricaoMessage = null
                            coroutineScope.launch {
                                try {
                                    val auth = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }
                                    val criancaId = when (auth?.user) {
                                        is com.oportunyfam_mobile.model.Crianca -> (auth.user as com.oportunyfam_mobile.model.Crianca).crianca_id
                                        else -> try {
                                            com.google.gson.Gson().toJson(auth?.user).let { com.google.gson.Gson().fromJson(it, com.oportunyfam_mobile.model.Crianca::class.java) }?.crianca_id
                                        } catch (e: Exception) { null }
                                    }

                                    if (criancaId == null) {
                                        inscricaoMessage = "Não foi possível identificar a criança logada"
                                        return@launch
                                    }

                                    val req = InscricaoRequest(id_crianca = criancaId, id_atividade = atividadeId, id_responsavel = null)
                                    val resp = withContext(Dispatchers.IO) { RetrofitFactory().getInscricaoService().criarInscricao(req).execute() }
                                    if (resp.isSuccessful) {
                                        inscricaoMessage = "Sugestão de inscrição enviada ao responsável"
                                        inscricaoViewModel.buscarInscricoesPorAtividade(atividadeId)
                                    } else {
                                        inscricaoMessage = "Erro ao sugerir inscrição: ${resp.code()}"
                                    }
                                } catch (e: Exception) {
                                    inscricaoMessage = "Falha na conexão"
                                    Log.e("AtividadeScreen", "Erro sugerir inscrição: ${e.message}", e)
                                }
                            }
                        }) {
                            Text("Sugerir Participação")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { showChildSuggestDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Fechar", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            }
        }

        // Mostrar mensagem de resultado (snackbar)
        inscricaoMessage?.let { msg ->
            LaunchedEffect(msg) {
                delay(3000)
                inscricaoMessage = null
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Snackbar { Text(msg) }
            }
        }
    }
}

@Composable
fun AtividadeDetailContent(
    atividade: AtividadeResponse,
    inscricoesState: com.oportunyfam_mobile.ViewModel.InscricoesState,
    onInscrever: () -> Unit,
    onParticipar: (Int) -> Unit,
    onSuggest: (Int) -> Unit,
    listState: LazyListState,
    currentAuthType: com.oportunyfam_mobile.data.AuthType?,
    currentChildId: Int?
) {
    val screenBg = Color(0xFFFFFFFF)
    val cardAccent = Color(0xFFFFF3E0)
    val primaryButton = Color(0xFFFFA000)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = screenBg),
        contentPadding = PaddingValues(bottom = 96.dp, top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState
    ) {
        // Header
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp), colors = CardDefaults.cardColors(containerColor = cardAccent)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!atividade.foto.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(atividade.foto).crossfade(true).build(),
                            contentDescription = atividade.titulo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = atividade.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = atividade.categoria, fontSize = 14.sp, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = atividade.descricao ?: "Sem descrição",
                                fontSize = 14.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.DarkGray
                            )
                        }

                        // Verificar se a criança logada já está inscrita
                        val childAlreadyEnrolled = currentChildId != null &&
                            (inscricoesState is com.oportunyfam_mobile.ViewModel.InscricoesState.Success &&
                            (inscricoesState.inscricoes.any { it.crianca_id == currentChildId && it.atividade_id == atividade.atividade_id }))

                        if (currentAuthType == com.oportunyfam_mobile.data.AuthType.CRIANCA && childAlreadyEnrolled) {
                            // Se a criança já está inscrita, mostrar status ao invés do botão
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Text(
                                    text = "Já inscrito",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        } else if (currentAuthType == com.oportunyfam_mobile.data.AuthType.CRIANCA && !childAlreadyEnrolled) {
                            // Criança não inscrita - botão de sugerir inscrição
                            Button(
                                onClick = { onSuggest(atividade.atividade_id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3), contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .height(44.dp)
                            ) {
                                Text(text = "Sugerir")
                            }
                        } else {
                            // Usuário normal - pode inscrever várias crianças
                            Button(
                                onClick = onInscrever,
                                colors = ButtonDefaults.buttonColors(containerColor = primaryButton, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .height(44.dp)
                            ) {
                                Text(text = "Inscrever")
                            }
                        }
                    }
                }
            }
        }

        // Aulas header
        item {
            Text(text = "Aulas", modifier = Modifier.padding(start = 16.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Aula items
        items(atividade.aulas) { aula ->
            val statusText = aula.status_aula ?: ""
            val isPast = statusText.contains("Encerrada", true) || statusText.contains("pass", true) || statusText.contains("encerr", true)
            val aulaCardColor = if (isPast) Color(0xFFF2F2F2) else cardAccent

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = aulaCardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = aula.data ?: "-", fontWeight = FontWeight.Bold, color = if (isPast) Color.Gray else Color.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "${aula.hora_inicio} - ${aula.hora_fim}", color = if (isPast) Color.Gray else Color.DarkGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Vagas: ${aula.vagas_disponiveis}/${aula.vagas_total}", color = if (isPast) Color.Gray else Color.DarkGray)
                    }

                    // Status indicator
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = if (isPast) "Encerrada" else (statusText.ifEmpty { "Ativa" }), color = if (isPast) Color(0xFF9E9E9E) else primaryButton, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        // If the class is in the future, allow joining (create matricula)
                        if (!isPast) {
                            // If logged as CRIANCA and the logged child is already enrolled, check both inscription and matricula
                            val childAlreadyEnrolled = currentChildId != null && (inscricoesState is com.oportunyfam_mobile.ViewModel.InscricoesState.Success &&
                                (inscricoesState.inscricoes.any { it.crianca_id == currentChildId && it.atividade_id == atividade.atividade_id }))

                            // Verificar se a criança já está matriculada NESTA AULA específica (em iram_participar, foram ou ausentes)
                            val childAlreadyMatriculada = currentChildId != null && (
                                aula.iram_participar?.any { it.crianca_id == currentChildId } == true ||
                                aula.foram?.any { it.crianca_id == currentChildId } == true ||
                                aula.ausentes?.any { it.crianca_id == currentChildId } == true
                            )

                            if (currentAuthType == com.oportunyfam_mobile.data.AuthType.CRIANCA) {
                                if (childAlreadyMatriculada) {
                                    // Se já está matriculada nesta aula específica
                                    Text(text = "Participando", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else if (childAlreadyEnrolled) {
                                    // Se está inscrita na atividade mas não nesta aula - pode participar
                                    Button(onClick = { onParticipar(aula.aula_id) }, colors = ButtonDefaults.buttonColors(containerColor = primaryButton)) {
                                        Text("Participar", color = Color.White)
                                    }
                                } else {
                                    // Não está inscrita - precisa sugerir inscrição primeiro
                                    Button(onClick = { onSuggest(atividade.atividade_id) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))) {
                                        Text("Sugerir", color = Color.White)
                                    }
                                }
                            } else {
                                // For users: direct participate (choose child)
                                Button(onClick = { onParticipar(aula.aula_id) }, colors = ButtonDefaults.buttonColors(containerColor = primaryButton)) {
                                    Text("Participar", color = Color.White)
                                }
                            }
                        }
                     }
                 }
             }
         }

        // Detalhes da atividade
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Detalhes da Atividade", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Local: ${atividade.cidade}, ${atividade.estado}", fontSize = 14.sp, color = Color.Black)
                    Text(text = "Faixa etária: ${atividade.faixa_etaria_min} - ${atividade.faixa_etaria_max} anos", fontSize = 14.sp, color = Color.Black)
                    Text(text = if (atividade.gratuita == 1) "Gratuita" else "Valor: R$ ${atividade.preco}", fontSize = 14.sp, color = Color.Black)
                }
            }
        }

        // Inscrições
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = "Inscrições", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                when (inscricoesState) {
                    is com.oportunyfam_mobile.ViewModel.InscricoesState.Loading -> CircularProgressIndicator()
                    is com.oportunyfam_mobile.ViewModel.InscricoesState.Error -> Text("Erro ao carregar inscrições", color = Color.Red)
                    is com.oportunyfam_mobile.ViewModel.InscricoesState.Success -> {
                        val inscricoes = (inscricoesState as com.oportunyfam_mobile.ViewModel.InscricoesState.Success).inscricoes
                        if (inscricoes.isEmpty()) {
                            Text("Nenhuma inscrição encontrada")
                        } else {
                            inscricoes.forEach { inscricao ->
                                val criancaNome = inscricao.crianca_nome
                                val criancaFoto = inscricao.crianca_foto
                                val status = inscricao.status_inscricao

                                Card(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (!criancaFoto.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current).data(criancaFoto).crossfade(true).build(),
                                                contentDescription = criancaNome,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(20.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.perfil),
                                                contentDescription = "Foto da criança",
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(20.dp)),
                                                colorFilter = ColorFilter.tint(Color.Gray)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = criancaNome, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            Text(
                                                text = when (status.uppercase()) {
                                                    "PENDENTE" -> "Inscrição pendente"
                                                    "CONFIRMADA" -> "Inscrição confirmada"
                                                    "CANCELADA" -> "Inscrição cancelada"
                                                    else -> "Status: $status"
                                                },
                                                fontSize = 14.sp,
                                                color = when (status.uppercase()) {
                                                    "PENDENTE" -> Color(0xFFFFA000)
                                                    "CONFIRMADA" -> Color(0xFF2E7D32)
                                                    "CANCELADA" -> Color(0xFFD32F2F)
                                                    else -> Color.Gray
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
