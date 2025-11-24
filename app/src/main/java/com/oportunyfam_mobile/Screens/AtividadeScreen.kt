package com.oportunyfam_mobile.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile.MainActivity
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.ViewModel.AtividadeViewModel
import com.oportunyfam_mobile.ViewModel.InscricaoViewModel
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.model.AulaDetalhe
import com.oportunyfam_mobile.model.AtividadeResponse
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.CriancaMini
import com.oportunyfam_mobile.model.InscricaoRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtividadeScreen(navController: NavHostController?, atividadeId: Int) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val atividadeViewModel: AtividadeViewModel = viewModel()
    val atividadeDetalheState by atividadeViewModel.atividadeDetalheState.collectAsState()

    val inscricaoViewModel: InscricaoViewModel = viewModel()
    val inscricoesState by inscricaoViewModel.inscricoesState.collectAsState()

    // Estados para inscrever
    var showInscreverDialog by remember { mutableStateOf(false) }
    var criancasList by remember { mutableStateOf<List<Crianca>>(emptyList()) }
    var isLoadingCriancas by remember { mutableStateOf(false) }
    var inscricaoMessage by remember { mutableStateOf<String?>(null) }

    val authDataStore = remember { AuthDataStore(context) }

    // Função suspensa para carregar as crianças do usuário (executada em coroutine)
    suspend fun loadCriancasDoUsuario(): List<Crianca> {
        return withContext(Dispatchers.IO) {
            try {
                val auth = authDataStore.loadAuthUser()
                val usuarioId = if (auth?.type == com.oportunyfam_mobile.data.AuthType.USUARIO) {
                    (auth.user as? com.oportunyfam_mobile.model.Usuario)?.usuario_id
                } else null

                if (usuarioId == null) return@withContext emptyList<Crianca>()

                val resp = RetrofitFactory().getUsuarioService().buscarPorId(usuarioId).execute()
                if (resp.isSuccessful) {
                    val usuarioResp = resp.body()?.usuario
                    val miniList = usuarioResp?.criancas_dependentes ?: emptyList()
                    return@withContext miniList.map { mini ->
                        Crianca(
                            crianca_id = mini.id_crianca,
                            pessoa_id = mini.id_pessoa,
                            nome = mini.nome,
                            email = null,
                            foto_perfil = null,
                            data_nascimento = "",
                            idade = 0,
                            criado_em = "",
                            atualizado_em = null,
                            sexo = null,
                            atividades_matriculadas = emptyList(),
                            conversas = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AtividadeScreen", "Erro ao carregar criancas: ${e.message}", e)
            }
            return@withContext emptyList<Crianca>()
        }
    }

    LaunchedEffect(atividadeId) {
        atividadeViewModel.buscarAtividadePorId(atividadeId)
        inscricaoViewModel.buscarInscricoesPorAtividade(atividadeId)
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Atividade") })
    }) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (atividadeDetalheState) {
                is com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                is com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Error -> {
                    val msg = (atividadeDetalheState as com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Error).message
                    Text(text = msg, color = Color.Red, modifier = Modifier.align(Alignment.TopCenter))
                }
                is com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Success -> {
                    val atividade = (atividadeDetalheState as com.oportunyfam_mobile.ViewModel.AtividadeDetalheState.Success).atividade
                    atividade?.let { atividadeData ->
                        AtividadeDetailContent(atividade = atividadeData, inscricoesState = inscricoesState, onInscrever = {
                            // Ao clicar no botão de inscrever: carregar crianças e abrir diálogo
                            coroutineScope.launch {
                                isLoadingCriancas = true
                                val list = loadCriancasDoUsuario()
                                criancasList = list
                                isLoadingCriancas = false
                                showInscreverDialog = true
                            }
                        })
                    }
                }
                else -> {
                    // Idle
                }
            }

            // Diálogo de seleção de criança e criação de inscrição
            if (showInscreverDialog) {
                AlertDialog(
                    onDismissRequest = { showInscreverDialog = false },
                    title = { Text("Escolha a criança") },
                    text = {
                        if (isLoadingCriancas) {
                            Column { CircularProgressIndicator() }
                        } else if (criancasList.isEmpty()) {
                            Text("Nenhuma criança disponível. Cadastre um filho primeiro.")
                        } else {
                            Column {
                                criancasList.forEach { c ->
                                    TextButton(onClick = {
                                        // criar inscricao
                                        showInscreverDialog = false
                                        inscricaoMessage = null

                                        // Determinar id_responsavel: se usuário logado for USUARIO, envia id_responsavel
                                        coroutineScope.launch {
                                            val auth = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }
                                            val idResponsavel = if (auth?.type == com.oportunyfam_mobile.data.AuthType.USUARIO) {
                                                (auth.user as? com.oportunyfam_mobile.model.Usuario)?.usuario_id
                                            } else null

                                            val request = InscricaoRequest(id_crianca = c.crianca_id, id_atividade = atividadeId, id_responsavel = idResponsavel)

                                            RetrofitFactory().getInscricaoService().criarInscricao(request).enqueue(object: Callback<com.oportunyfam_mobile.model.InscricaoCriadaResponse> {
                                                override fun onResponse(call: Call<com.oportunyfam_mobile.model.InscricaoCriadaResponse>, response: Response<com.oportunyfam_mobile.model.InscricaoCriadaResponse>) {
                                                    if (response.isSuccessful) {
                                                        inscricaoMessage = "Inscrição solicitada com sucesso"
                                                        inscricaoViewModel.buscarInscricoesPorAtividade(atividadeId)
                                                    } else {
                                                        inscricaoMessage = "Erro ao inscrever: ${response.code()}"
                                                    }
                                                }

                                                override fun onFailure(call: Call<com.oportunyfam_mobile.model.InscricaoCriadaResponse>, t: Throwable) {
                                                    inscricaoMessage = "Falha na conexão"
                                                }
                                            })
                                        }

                                    }) { Text(text = c.nome) }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showInscreverDialog = false }) { Text("Fechar") }
                    }
                )
            }

            // Mostrar mensagem de resultado
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
}

@Composable
fun AtividadeDetailContent(atividade: AtividadeResponse, inscricoesState: com.oportunyfam_mobile.ViewModel.InscricoesState, onInscrever: () -> Unit) {
    val gradient = Brush.horizontalGradient(listOf(Color(0xFFFFA000), Color(0xFFFFD27A)))

    Column(modifier = Modifier.fillMaxSize().background(brush = gradient)) {
        // Header
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = atividade.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = atividade.categoria, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = atividade.descricao ?: "Sem descrição", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Aulas
        Text(text = "Aulas", modifier = Modifier.padding(start = 16.dp), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        when (inscricoesState) {
            is com.oportunyfam_mobile.ViewModel.InscricoesState.Loading -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is com.oportunyfam_mobile.ViewModel.InscricoesState.Error -> Text(text = inscricoesState.message, color = Color.Red)
            is com.oportunyfam_mobile.ViewModel.InscricoesState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(atividade.aulas) { aula ->
                        AulaItem(aula = aula)
                    }
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onInscrever, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Inscrever criança")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AulaItem(aula: AulaDetalhe) {
    Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = aula.data ?: "Data não informada", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Início: ${aula.hora_inicio} - Fim: ${aula.hora_fim}")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Vagas disponíveis: ${aula.vagas_disponiveis}")
        }
    }
}
