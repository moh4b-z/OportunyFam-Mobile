package com.oportunyfam_mobile.Screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.oportunyfam_mobile.Components.CriarPublicacaoDialog
import com.oportunyfam_mobile.Components.EditarPublicacaoDialog
import com.oportunyfam_mobile.Components.PublicacoesGrid
import com.oportunyfam_mobile.MainActivity.NavRoutes
import com.oportunyfam_mobile.Service.AzureBlobRetrofit
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.data.AuthType
import com.oportunyfam_mobile.model.getRealPathFromURI
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.ViewModel.PublicacaoViewModel
import com.oportunyfam_mobile.ViewModel.PublicacoesState
import com.oportunyfam_mobile.ViewModel.CriarPublicacaoState
import com.oportunyfam_mobile.ViewModel.EditarPublicacaoState
import com.oportunyfam_mobile.model.Publicacao
import com.oportunyfam_mobile.model.InstituicaoAtualizarRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.model.Instituicao

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

    // ViewModel de Publicações
    val publicacaoViewModel: PublicacaoViewModel = viewModel()
    val publicacoesState by publicacaoViewModel.publicacoesState.collectAsState()
    val criarPublicacaoState by publicacaoViewModel.criarPublicacaoState.collectAsState()
    val editarPublicacaoState by publicacaoViewModel.editarPublicacaoState.collectAsState()

    var isLoadingUpdate by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var novaDescricao by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var tempImageFile by remember { mutableStateOf<File?>(null) }

    // Estados para criar publicação
    var showPublicacaoDialog by remember { mutableStateOf(false) }
    var publicacaoDescricao by remember { mutableStateOf("") }
    var publicacaoImageFile by remember { mutableStateOf<File?>(null) }
    var isUploadingPublicacao by remember { mutableStateOf(false) }

    // Estados para editar publicação
    var showEditarPublicacaoDialog by remember { mutableStateOf(false) }
    var publicacaoParaEditar by remember { mutableStateOf<Publicacao?>(null) }
    var editarPublicacaoDescricao by remember { mutableStateOf("") }
    var isEditingPublicacao by remember { mutableStateOf(false) }


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

    // Observar estado de criação de publicação
    LaunchedEffect(criarPublicacaoState) {
        when (criarPublicacaoState) {
            is CriarPublicacaoState.Success -> {
                Log.d("PerfilScreen", "✅ Publicação criada com sucesso - limpando estado do diálogo")
                snackbarMessage = "Publicação criada com sucesso!"
                showSnackbar = true
                // Limpar estado do diálogo
                publicacaoDescricao = ""
                publicacaoImageFile = null
                showPublicacaoDialog = false
                isUploadingPublicacao = false
                publicacaoViewModel.limparEstadoCriacao()
            }
            is CriarPublicacaoState.Error -> {
                Log.e("PerfilScreen", "❌ Erro ao criar publicação: ${(criarPublicacaoState as CriarPublicacaoState.Error).message}")
                snackbarMessage = (criarPublicacaoState as CriarPublicacaoState.Error).message
                showSnackbar = true
                isUploadingPublicacao = false
                publicacaoViewModel.limparEstadoCriacao()
            }
            else -> {}
        }
    }

    // Observar estado de edição de publicação
    LaunchedEffect(editarPublicacaoState) {
        when (editarPublicacaoState) {
            is EditarPublicacaoState.Success -> {
                Log.d("PerfilScreen", "✅ Publicação editada com sucesso")
                snackbarMessage = "Publicação editada com sucesso!"
                showSnackbar = true
                // Limpar estado do diálogo
                editarPublicacaoDescricao = ""
                publicacaoParaEditar = null
                showEditarPublicacaoDialog = false
                isEditingPublicacao = false
                publicacaoViewModel.limparEstadoEdicao()
            }
            is EditarPublicacaoState.Error -> {
                Log.e("PerfilScreen", "❌ Erro ao editar publicação: ${(editarPublicacaoState as EditarPublicacaoState.Error).message}")
                snackbarMessage = (editarPublicacaoState as EditarPublicacaoState.Error).message
                showSnackbar = true
                isEditingPublicacao = false
                publicacaoViewModel.limparEstadoEdicao()
            }
            else -> {}
        }
    }

    // ============================================
    // FUNÇÕES DE NEGÓCIO
    // ============================================
    val uploadAndUpdateProfileImage: () -> Unit = {
        tempImageFile?.let { imageFile ->
            isLoadingUpdate = true
            scope.launch {
                try {
                    // Verifica se Azure está configurado
                    if (!com.oportunyfam_mobile.Config.AzureConfig.isConfigured()) {
                        val errorMessage = "Upload de imagens não está configurado"
                        android.util.Log.w("PerfilScreen", "⚠️ Azure Storage não configurado. Upload de imagens desabilitado.")
                        isLoadingUpdate = false
                        return@launch
                    }

                    val accountKey = com.oportunyfam_mobile.Config.AzureConfig.getStorageKey()!!

                    Log.d("PerfilScreen", "Iniciando upload da imagem...")

                    val imageUrl = AzureBlobRetrofit.uploadImageToAzure(
                        imageFile,
                        com.oportunyfam_mobile.Config.AzureConfig.STORAGE_ACCOUNT,
                        accountKey,
                        com.oportunyfam_mobile.Config.AzureConfig.CONTAINER_PERFIL
                    )

                    Log.d("PerfilScreen", "Upload retornou URL: $imageUrl")

                    if (imageUrl != null && instituicao != null) {
                        val instituicaoService = RetrofitFactory().getInstituicaoService()
                        val currentInstituicao = instituicao!!
                        val versionedUrl = "$imageUrl?v=${System.currentTimeMillis()}"

                        val updateRequest = InstituicaoAtualizarRequest(
                            nome = currentInstituicao.nome,
                            foto_perfil = versionedUrl,
                            cnpj = currentInstituicao.cnpj,
                            telefone = currentInstituicao.telefone,
                            email = currentInstituicao.email,
                            descricao = currentInstituicao.descricao ?: ""
                        )

                        val response = instituicaoService.atualizar(currentInstituicao.instituicao_id, updateRequest)

                        when {
                            response.isSuccessful -> {
                                Log.d("PerfilScreen", "✅ Foto de perfil atualizada com sucesso!")
                                val updatedInstituicao = currentInstituicao.copy(foto_perfil = versionedUrl)
                                // TODO: Salvar instituição no AuthDataStore quando disponível
                                // Aguardar para garantir que o DataStore salvou
                                delay(200)
                                // Incrementar trigger para forçar LaunchedEffect a recarregar
                                reloadTrigger++
                                Log.d("PerfilScreen", "🔄 Trigger de reload incrementado para: $reloadTrigger")
                                snackbarMessage = "Foto de perfil atualizada com sucesso!"
                                showSnackbar = true
                            }
                            response.code() == 429 -> {
                                Log.w("PerfilScreen", "⚠️ Rate limit - salvando localmente")
                                val updatedInstituicao = currentInstituicao.copy(foto_perfil = versionedUrl)
                                // TODO: Salvar instituição atualizada no AuthDataStore quando disponível
                                // Aguardar para garantir que o DataStore salvou
                                delay(200)
                                // Incrementar trigger para forçar LaunchedEffect a recarregar
                                reloadTrigger++
                                Log.d("PerfilScreen", "🔄 Trigger de reload incrementado para: $reloadTrigger")
                                snackbarMessage = "Foto salva! Servidor ocupado, sincronizará depois."
                                showSnackbar = true
                            }
                            else -> {
                                snackbarMessage = "Erro ao atualizar (${response.code()})"
                                showSnackbar = true
                            }
                        }
                    } else {
                        snackbarMessage = "Erro ao fazer upload da imagem"
                        showSnackbar = true
                    }
                } catch (e: Exception) {
                    Log.e("PerfilScreen", "Erro no upload: ${e.message}", e)
                    snackbarMessage = "Erro: ${e.message}"
                    showSnackbar = true
                } finally {
                    isLoadingUpdate = false
                    tempImageFile = null
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("PerfilScreen", "Imagem selecionada: $it")

            val filePath = context.getRealPathFromURI(it)
            filePath?.let { path ->
                tempImageFile = File(path)
                Log.d("PerfilScreen", "Arquivo preparado: ${tempImageFile?.name}")
                uploadAndUpdateProfileImage()
            } ?: run {
                snackbarMessage = "Erro ao processar a imagem"
                showSnackbar = true
            }
        }
    }

    // Launcher para selecionar imagem de publicação
    val publicacaoImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("PerfilScreen", "Imagem de publicação selecionada: $it")
            val filePath = context.getRealPathFromURI(it)
            filePath?.let { path ->
                publicacaoImageFile = File(path)
                Log.d("PerfilScreen", "Arquivo de publicação preparado: ${publicacaoImageFile?.name}")
            } ?: run {
                snackbarMessage = "Erro ao processar a imagem"
                showSnackbar = true
            }
        }
    }

    val onLogout: () -> Unit = {
        scope.launch {
            authDataStore.logout()
            navController?.navigate(NavRoutes.REGISTRO) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    val onEditDescription: () -> Unit = {
        novaDescricao = instituicao?.descricao ?: ""
        showEditDialog = true
    }

    val onSaveDescription: () -> Unit = {
        if (novaDescricao.isNotBlank() && instituicao != null) {
            isLoadingUpdate = true
            scope.launch {
                try {
                    val instituicaoService = RetrofitFactory().getInstituicaoService()
                    val currentInstituicao = instituicao!!

                    val updateRequest = InstituicaoAtualizarRequest(
                        nome = currentInstituicao.nome,
                        foto_perfil = currentInstituicao.foto_perfil,
                        cnpj = currentInstituicao.cnpj,
                        telefone = currentInstituicao.telefone,
                        email = currentInstituicao.email,
                        descricao = novaDescricao
                    )

                    val response = instituicaoService.atualizar(currentInstituicao.instituicao_id, updateRequest)

                    if (response.isSuccessful) {
                        val updatedInstituicao = currentInstituicao.copy(descricao = novaDescricao)
                        // TODO: Salvar instituição atualizada no AuthDataStore quando disponível
                        delay(200)
                        // Incrementar trigger para forçar reload
                        reloadTrigger++
                        showEditDialog = false
                        snackbarMessage = "Descrição atualizada com sucesso!"
                        showSnackbar = true
                    } else {
                        snackbarMessage = "Erro ao atualizar: ${response.code()}"
                        showSnackbar = true
                    }
                } catch (e: Exception) {
                    snackbarMessage = "Erro de conexão: ${e.message}"
                    showSnackbar = true
                } finally {
                    isLoadingUpdate = false
                }
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

                        // Botão de câmera para alterar foto de perfil
                        FloatingActionButton(
                            onClick = { if (!isLoadingUpdate) imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .offset(x = 4.dp, y = 4.dp),
                            containerColor = Color(0xFFFFA000),
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            if (isLoadingUpdate) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Alterar foto de perfil",
                                    modifier = Modifier.size(20.dp)
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

                    // Descrição da Instituição com botão de editar
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            instituicao?.descricao ?: "Nenhuma descrição disponível. Clique no ícone para editar.",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(end = 32.dp)
                        )

                        // Botão de editar
                        IconButton(
                            onClick = onEditDescription,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Editar descrição",
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(20.dp)
                            )
                        }
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

                        // Botão Adicionar Publicação
                        FloatingActionButton(
                            onClick = { showPublicacaoDialog = true },
                            modifier = Modifier.size(48.dp),
                            containerColor = Color(0xFFFFA000),
                            contentColor = Color.White
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Adicionar Publicação",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid de Publicações da API
                    PublicacoesGrid(
                        publicacoesState = publicacoesState,
                        instituicaoIdLogada = instituicaoId, // Passa o ID da instituição logada
                        onDeletePublicacao = { publicacaoId ->
                            instituicao?.let {
                                publicacaoViewModel.deletarPublicacao(publicacaoId, it.instituicao_id)
                            }
                        },
                        onEditPublicacao = { publicacao ->
                            Log.d("PerfilScreen", "✏️ Iniciando edição da publicação: ${publicacao.id}")
                            publicacaoParaEditar = publicacao
                            editarPublicacaoDescricao = publicacao.descricao ?: ""
                            showEditarPublicacaoDialog = true
                        }
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

    // Diálogo de edição de descrição
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoadingUpdate) {
                    showEditDialog = false
                }
            },
            title = { Text("Editar Descrição") },
            text = {
                Column {
                    Text("Digite a nova descrição da sua instituição:")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = novaDescricao,
                        onValueChange = { novaDescricao = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Descreva sua instituição...") },
                        maxLines = 5,
                        enabled = !isLoadingUpdate
                    )
                }
            },
            confirmButton = {
                if (isLoadingUpdate) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    TextButton(
                        onClick = onSaveDescription,
                        enabled = novaDescricao.isNotBlank() && !isLoadingUpdate
                    ) {
                        Text("Salvar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    enabled = !isLoadingUpdate
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de criar publicação
    if (showPublicacaoDialog) {
        CriarPublicacaoDialog(
            descricao = publicacaoDescricao,
            imagemSelecionada = publicacaoImageFile != null,
            isLoading = isUploadingPublicacao,
            onDescricaoChange = { publicacaoDescricao = it },
            onSelecionarImagem = { publicacaoImagePickerLauncher.launch("image/*") },
            onSalvar = {
                if (publicacaoDescricao.trim().length >= 30 && publicacaoImageFile != null && instituicao != null) {
                    isUploadingPublicacao = true
                    scope.launch {
                        try {
                            // Verifica se Azure está configurado
                            if (!com.oportunyfam_mobile.Config.AzureConfig.isConfigured()) {
                                android.util.Log.w("PerfilScreen", "⚠️ Azure Storage não configurado. Upload de publicações desabilitado.")
                                isUploadingPublicacao = false
                                return@launch
                            }

                            val accountKey = com.oportunyfam_mobile.Config.AzureConfig.getStorageKey()!!

                            Log.d("PerfilScreen", "📤 Fazendo upload da imagem da publicação...")

                            val imageUrl = AzureBlobRetrofit.uploadImageToAzure(
                                publicacaoImageFile!!,
                                com.oportunyfam_mobile.Config.AzureConfig.STORAGE_ACCOUNT,
                                accountKey,
                                com.oportunyfam_mobile.Config.AzureConfig.CONTAINER_PUBLICACOES
                            )

                            if (imageUrl != null) {
                                Log.d("PerfilScreen", "✅ Upload concluído: $imageUrl")
                                Log.d("PerfilScreen", "📝 Criando publicação na API...")

                                // Add cache-busting parameter to image URL
                                val versionedImageUrl = "$imageUrl?v=${System.currentTimeMillis()}"
                                Log.d("PerfilScreen", "🔄 URL com cache-busting: $versionedImageUrl")

                                publicacaoViewModel.criarPublicacao(
                                    descricao = publicacaoDescricao,
                                    imagem = versionedImageUrl,
                                    instituicaoId = instituicao!!.instituicao_id
                                )

                                // Don't clean up here - let LaunchedEffect handle it based on API response
                                Log.d("PerfilScreen", "⏳ Aguardando resposta da API...")
                            } else {
                                snackbarMessage = "Erro ao fazer upload da imagem"
                                showSnackbar = true
                                isUploadingPublicacao = false
                            }
                        } catch (e: Exception) {
                            Log.e("PerfilScreen", "❌ Erro ao criar publicação", e)
                            snackbarMessage = "Erro: ${e.message}"
                            showSnackbar = true
                            isUploadingPublicacao = false
                        }
                    }
                }
            },
            onDismiss = {
                if (!isUploadingPublicacao) {
                    showPublicacaoDialog = false
                    publicacaoDescricao = ""
                    publicacaoImageFile = null
                }
            }
        )
    }

    // Diálogo de editar publicação
    if (showEditarPublicacaoDialog && publicacaoParaEditar != null) {
        EditarPublicacaoDialog(
            publicacao = publicacaoParaEditar!!,
            descricao = editarPublicacaoDescricao,
            isLoading = isEditingPublicacao,
            onDescricaoChange = { editarPublicacaoDescricao = it },
            onSalvar = {
                if (editarPublicacaoDescricao.trim().length >= 30 && instituicao != null && publicacaoParaEditar != null) {
                    isEditingPublicacao = true
                    Log.d("PerfilScreen", "💾 Salvando edição da publicação ID: ${publicacaoParaEditar!!.id}")

                    // Usar a imagem existente com novo cache-busting
                    val imageUrl = publicacaoParaEditar!!.imagem
                    val versionedImageUrl = if (imageUrl != null) {
                        // Remove antigo cache-busting se existir
                        val cleanUrl = imageUrl.split("?")[0]
                        "$cleanUrl?v=${System.currentTimeMillis()}"
                    } else {
                        null
                    }

                    publicacaoViewModel.editarPublicacao(
                        publicacaoId = publicacaoParaEditar!!.id,
                        descricao = editarPublicacaoDescricao,
                        imagem = versionedImageUrl,
                        instituicaoId = instituicao!!.instituicao_id
                    )
                }
            },
            onDismiss = {
                if (!isEditingPublicacao) {
                    showEditarPublicacaoDialog = false
                    publicacaoParaEditar = null
                    editarPublicacaoDescricao = ""
                }
            }
        )
    }
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