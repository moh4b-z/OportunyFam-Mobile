package com.oportunyfam_mobile.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.PublicacoesGrid
import com.oportunyfam_mobile.MainActivity.NavRoutes
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.ViewModel.AtividadeViewModel
import com.oportunyfam_mobile.ViewModel.PublicacaoViewModel
import com.oportunyfam_mobile.ViewModel.PublicacoesState
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.model.AtividadeResponse
import com.oportunyfam_mobile.model.Instituicao
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================
// SCREEN PRINCIPAL
// ============================================


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilOngScreen(navController: NavHostController?, instituicaoId: Int = 0) {
    // ============================================
    // INICIALIZAÇÃO E ESTADOS
    // ============================================
    val context = LocalContext.current
    val authDataStore = remember { AuthDataStore(context) }
    val scope = rememberCoroutineScope()
    var instituicao by remember { mutableStateOf<Instituicao?>(null) }
    var reloadTrigger by remember { mutableIntStateOf(0) } // Trigger para forçar reload

    // Carregar instituição quando o ID for passado
    LaunchedEffect(instituicaoId, reloadTrigger) {
        if (instituicaoId > 0) {
            Log.d("PerfilOngScreen", "🔄 Carregando instituição ID=$instituicaoId (trigger=$reloadTrigger)...")
            scope.launch {
                try {
                    val response = RetrofitFactory().getInstituicaoService().buscarPorIdSuspend(instituicaoId)
                    if (response.isSuccessful) {
                        instituicao = response.body()?.instituicao
                        Log.d("PerfilOngScreen", "✅ Instituição carregada: ${instituicao?.nome}")
                    } else {
                        Log.e("PerfilOngScreen", "❌ Erro ao buscar instituição: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("PerfilOngScreen", "❌ Erro ao buscar instituição", e)
                }
            }
        } else {
            Log.d("PerfilOngScreen", "⚠️ Nenhum ID de instituição fornecido")
        }
    }

    // ViewModel de Publicações (somente leitura)
    val publicacaoViewModel: PublicacaoViewModel = viewModel()
    val publicacoesState by publicacaoViewModel.publicacoesState.collectAsState()

    // ViewModel de Atividades
    val atividadeViewModel: AtividadeViewModel = viewModel()
    val atividadesState by atividadeViewModel.atividadesState.collectAsState()

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    // ============================================
    // CARREGAMENTO INICIAL E RECARREGAMENTO
    // ============================================
    // Carregar publicações quando instituição estiver disponível
    LaunchedEffect(instituicao) {
        instituicao?.let {
            Log.d("PerfilScreen", "🔍 Carregando publicações da instituição: ${it.instituicao_id}")
            Log.d("PerfilScreen", "📸 URL da foto atual: ${it.foto_perfil}")
            publicacaoViewModel.buscarPublicacoesPorInstituicao(it.instituicao_id)
        }
    }

    // Carregar atividades ao ter a instituição
    LaunchedEffect(instituicao) {
        instituicao?.let { atividadeViewModel.buscarAtividadesPorInstituicao(it.instituicao_id) }
    }

    // ============================================
    // FUNÇÕES DE NEGÓCIO
    // ============================================

    val onLogout: () -> Unit = {
        scope.launch {
            authDataStore.logout()
            navController?.navigate(NavRoutes.REGISTRO) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    // ============================================
    // ESTADOS DE CARREGAMENTO
    // ============================================
    if (instituicao == null) {
        LaunchedEffect(Unit) {
            delay(3000)
            if (instituicao == null) {
                Log.w("PerfilScreen", "Timeout - redirecionando para login")
                onLogout()
            }
        }
        LoadingScreen(message = "Carregando perfil...")
        return
    }

    // ============================================
    // UI PRINCIPAL
    // ============================================
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),
            Color(0xFFFFD27A)
        )
    )

    // Dados da instituição
    val instituicaoNome = instituicao?.nome ?: "Instituição Não Encontrada"
    val instituicaoEmail = instituicao?.email ?: "email@exemplo.com"

    // Snackbar para mostrar mensagens
    if (showSnackbar) {
        LaunchedEffect(showSnackbar) {
            delay(3000)
            showSnackbar = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showSnackbar = false }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(snackbarMessage, color = Color.White)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair", tint = Color.Black)
            }

            IconButton(onClick = { /* Ação para Notificações */ }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificações", tint = Color.Black)
            }
            IconButton(onClick = { /* Ação para Menu */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black)
            }
        }



        Spacer(modifier = Modifier.height(60.dp))

        // Conteúdo principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Tornar toda a área do card rolável: usar LazyColumn para o conteúdo do card
            var selectedTab by remember { mutableStateOf("Publicações") }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    // Header do perfil (foto, nome, email, endereço, descrição)
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(120.dp)) {
                            Card(shape = CircleShape, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                                val fotoPerfilUrl = instituicao?.foto_perfil
                                if (!fotoPerfilUrl.isNullOrEmpty()) {
                                    key(fotoPerfilUrl, reloadTrigger) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(fotoPerfilUrl)
                                                .crossfade(true)
                                                .diskCachePolicy(CachePolicy.DISABLED)
                                                .memoryCachePolicy(CachePolicy.DISABLED)
                                                .build(),
                                            contentDescription = "Foto de perfil da instituição",
                                            contentScale = ContentScale.Crop,
                                            placeholder = painterResource(id = R.drawable.perfil),
                                            error = painterResource(id = R.drawable.perfil),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.perfil),
                                        contentDescription = "Foto de perfil padrão",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = instituicao?.nome ?: "Instituição Não Encontrada", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = instituicao?.email ?: "", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        instituicao?.endereco?.let { endereco ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Localização", tint = Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = buildString {
                                        append(endereco.logradouro)
                                        if (!endereco.numero.isNullOrEmpty()) append(", ${endereco.numero}")
                                    }, fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "${endereco.bairro} - ${endereco.cidade}, ${endereco.estado}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(text = instituicao?.descricao ?: "Nenhuma descrição disponível.", fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp, modifier = Modifier.padding(horizontal = 16.dp))

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))

                        // Abas
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = { selectedTab = "Publicações" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "Publicações") Color(0xFFFFA000) else Color.LightGray, contentColor = if (selectedTab == "Publicações") Color.White else Color.DarkGray), shape = RoundedCornerShape(20.dp), modifier = Modifier.height(40.dp)) { Text(text = "Publicações", fontSize = 13.sp) }
                            Button(onClick = { selectedTab = "Atividades" }, colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "Atividades") Color(0xFFFFA000) else Color.LightGray, contentColor = if (selectedTab == "Atividades") Color.White else Color.DarkGray), shape = RoundedCornerShape(20.dp), modifier = Modifier.height(40.dp)) { Text(text = "Atividades", fontSize = 13.sp) }
                        }
                    }
                }

                // Conteúdo da aba
                if (selectedTab == "Publicações") {
                    item {
                        PublicacoesGrid(publicacoesState = publicacoesState, instituicaoIdLogada = null, onDeletePublicacao = {}, onEditPublicacao = {})
                    }
                } else {
                    when (atividadesState) {
                        is com.oportunyfam_mobile.ViewModel.AtividadesState.Loading -> item {
                            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFFA000)) }
                        }
                        is com.oportunyfam_mobile.ViewModel.AtividadesState.Error -> item {
                            val msg = (atividadesState as com.oportunyfam_mobile.ViewModel.AtividadesState.Error).message
                            Text(text = msg, color = Color.Red)
                        }
                        is com.oportunyfam_mobile.ViewModel.AtividadesState.Success -> {
                            val atividades = (atividadesState as com.oportunyfam_mobile.ViewModel.AtividadesState.Success).atividades
                            if (atividades.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { Text("Nenhuma atividade encontrada", color = Color.Gray) }
                                }
                            } else {
                                items(atividades) { atividade ->
                                    AtividadeCard(atividade = atividade, onClick = {
                                        navController?.navigate("atividade/${atividade.atividade_id}")
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }

        // Barra de Tarefas com fundo contínuo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFA000))
        ) {
            BarraTarefas(
                navController = navController,
                currentRoute = NavRoutes.HOME
            )
        }
    }

    // Edições (diálogos) removidas — a tela agora é somente leitura.
}


// ============================================
// COMPONENTE DE LOADING
// ============================================

@Composable
private fun LoadingScreen(message: String = "Carregando...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFFA000))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AtividadeCard(atividade: AtividadeResponse, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .padding(horizontal = 0.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Imagem lateral (se existir)
            if (!atividade.foto.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(atividade.foto).crossfade(true).build(),
                    contentDescription = atividade.titulo,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF2F2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.fillMaxHeight().weight(1f)) {
                Text(text = atividade.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                if (!atividade.descricao.isNullOrEmpty()) {
                    Text(text = atividade.descricao, fontSize = 13.sp, color = Color.DarkGray, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Idade: ${atividade.faixa_etaria_min}-${atividade.faixa_etaria_max}", fontSize = 12.sp, color = Color.Gray)
                    Text(text = if (atividade.gratuita == 1) "Gratuita" else "R$ ${atividade.preco}", fontSize = 12.sp, color = Color(0xFFFFA000), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Preview(showSystemUi = true)
@Composable
fun PerfilOngScreenPreview() {
    PerfilOngScreen(navController = null)
}