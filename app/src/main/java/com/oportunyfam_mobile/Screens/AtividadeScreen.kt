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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

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
                    AtividadeDetailContent(atividade = atividade, inscricoesState = inscricoesState, onInscrever = {
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

@Composable
fun AtividadeDetailContent(atividade: AtividadeResponse, inscricoesState: com.oportunyfam_mobile.ViewModel.InscricoesState, onInscrever: () -> Unit) {
    val screenBg = Color(0xFFFFFFFF)
    val cardAccent = Color(0xFFFFF3E0)
    val primaryButton = Color(0xFFFFA000)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = screenBg),
        contentPadding = PaddingValues(bottom = 96.dp, top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Text(text = if (isPast) "Encerrada" else (statusText.ifEmpty { "Ativa" }), color = if (isPast) Color(0xFF9E9E9E) else primaryButton, fontWeight = FontWeight.Bold)
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
