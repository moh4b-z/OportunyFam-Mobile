package com.oportunyfam_mobile.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.oportunyfam_mobile.ViewModel.PublicacaoViewModel
import com.oportunyfam_mobile.ViewModel.PublicacoesState
import com.oportunyfam_mobile.R
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Informações do perfil
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Foto de Perfil da Instituição (VEM DA API)
                    Box(
                        modifier = Modifier.size(120.dp)
                    ) {
                        Card(
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            val fotoPerfilUrl = instituicao?.foto_perfil
                            Log.d("PerfilScreen", "🖼️ Renderizando foto: $fotoPerfilUrl (trigger=$reloadTrigger)")

                            if (!fotoPerfilUrl.isNullOrEmpty()) {
                                // key() com URL e trigger força recomposição quando qualquer um mudar
                                key(fotoPerfilUrl, reloadTrigger) {
                                    Log.d("PerfilScreen", "🔄 AsyncImage sendo recomposta com key: $fotoPerfilUrl")
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

                    // Nome da Instituição
                    Text(
                        instituicaoNome,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Email da Instituição
                    Text(
                        instituicaoEmail,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Endereço da Instituição
                    instituicao?.endereco?.let { endereco ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Localização",
                                    tint = Color(0xFFFFA000),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    buildString {
                                        append(endereco.logradouro)
                                        if (!endereco.numero.isNullOrEmpty()) {
                                            append(", ${endereco.numero}")
                                        }
                                    },
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${endereco.bairro} - ${endereco.cidade}, ${endereco.estado}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Descrição da Instituição
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            instituicao?.descricao ?: "Nenhuma descrição disponível.",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(end = 32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Divisor
                    HorizontalDivider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Seção de Publicações (Fotos da Instituição)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Publicações",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid de Publicações da API
                    PublicacoesGrid(
                        publicacoesState = publicacoesState,
                        instituicaoIdLogada = null, // desativa edição no componente
                        onDeletePublicacao = {},
                        onEditPublicacao = {}
                    )
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
                currentRoute = NavRoutes.PERFIL
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

@Preview(showSystemUi = true)
@Composable
fun PerfilOngScreenPreview() {
    PerfilOngScreen(navController = null)
}