package com.oportunyfam_mobile.Screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.data.AuthType
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.Usuario
import kotlinx.coroutines.launch

private const val TAG = "PerfilScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavHostController?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authDataStore = remember { AuthDataStore(context) }

    // Estados principais
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var crianca by remember { mutableStateOf<Crianca?>(null) }
    var isCrianca by remember { mutableStateOf(false) }
    var filhos by remember { mutableStateOf<List<Crianca>>(emptyList()) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    // Estados de UI
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var isUploadingFoto by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Informações") }
    var showAddChildDialog by remember { mutableStateOf(false) }

    // Launcher para selecionar foto
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploadingFoto = true
                try {
                    uploadFotoPerfil(
                        context = context,
                        uri = it,
                        isCrianca = isCrianca,
                        usuario = usuario,
                        crianca = crianca,
                        onSuccess = { novaUrl ->
                            snackbarMessage = "Foto atualizada com sucesso!"
                            showSnackbar = true
                            reloadTrigger++
                        },
                        onError = { erro ->
                            snackbarMessage = "Erro: $erro"
                            showSnackbar = true
                        }
                    )
                } finally {
                    isUploadingFoto = false
                }
            }
        }
    }

    // Carregar dados do perfil
    LaunchedEffect(reloadTrigger) {
        Log.d(TAG, "🔄 Carregando perfil (trigger=$reloadTrigger)...")
        val authData = authDataStore.loadAuthUser()

        if (authData != null) {
            Log.d(TAG, "✅ Tipo de usuário: ${authData.type}")

            when (authData.type) {
                AuthType.USUARIO -> {
                    isCrianca = false
                    usuario = authData.user as? Usuario
                    Log.d(TAG, "👤 Usuário carregado: ${usuario?.nome}")

                    // Buscar filhos
                    usuario?.usuario_id?.let { usuarioId ->
                        try {
                            RetrofitFactory().getCriancaService().listarPorUsuario(usuarioId).enqueue(
                                object : retrofit2.Callback<com.oportunyfam_mobile.model.CriancaListResponse> {
                                    override fun onResponse(
                                        call: retrofit2.Call<com.oportunyfam_mobile.model.CriancaListResponse>,
                                        response: retrofit2.Response<com.oportunyfam_mobile.model.CriancaListResponse>
                                    ) {
                                        if (response.isSuccessful) {
                                            filhos = (response.body()?.criancas ?: emptyList()) as List<Crianca>
                                            Log.d(TAG, "👶 Filhos carregados: ${filhos.size}")
                                        }
                                    }

                                    override fun onFailure(
                                        call: retrofit2.Call<com.oportunyfam_mobile.model.CriancaListResponse>,
                                        t: Throwable
                                    ) {
                                        Log.e(TAG, "Erro ao buscar filhos", t)
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Erro ao buscar filhos", e)
                        }
                    }
                }
                AuthType.CRIANCA -> {
                    isCrianca = true
                    crianca = authData.user as? Crianca
                    Log.d(TAG, "👶 Criança carregada: ${crianca?.nome}")
                }
            }
        } else {
            Log.w(TAG, "⚠️ Nenhum usuário logado")
        }
    }

    // Função de logout
    fun onLogout() {
        scope.launch {
            authDataStore.logout()
            navController?.navigate("login") {
                popUpTo(0)
            }
        }
    }

    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFA000), Color(0xFFFFD27A))
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
                IconButton(onClick = { onLogout() }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair", tint = Color.Black)
                }
            }

            // Conteúdo principal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Card branco principal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .align(Alignment.BottomCenter),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 100.dp)
                    ) {
                        item {
                            // Nome
                            Text(
                                text = if (isCrianca) crianca?.nome ?: "Criança" else usuario?.nome ?: "Usuário",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Email
                            Text(
                                text = if (isCrianca) crianca?.email ?: "" else usuario?.email ?: "",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Tabs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TabButton(
                                    text = "Informações",
                                    isSelected = selectedTab == "Informações",
                                    onClick = { selectedTab = "Informações" }
                                )

                                TabButton(
                                    text = if (isCrianca) "Responsáveis" else "Filhos",
                                    isSelected = selectedTab == if (isCrianca) "Responsáveis" else "Filhos",
                                    onClick = { selectedTab = if (isCrianca) "Responsáveis" else "Filhos" }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Conteúdo das tabs
                        item {
                            when (selectedTab) {
                                "Informações" -> {
                                    InformacoesTab(usuario = usuario, crianca = crianca, isCrianca = isCrianca)
                                }
                                "Filhos" -> {
                                    FilhosTab(filhos = filhos, navController = navController)
                                }
                                "Responsáveis" -> {
                                    ResponsaveisTab()
                                }
                            }
                        }
                    }
                }

                // Foto de perfil sobreposta
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-50).dp)
                ) {
                    val fotoUrl = if (isCrianca) crianca?.foto_perfil else usuario?.foto_perfil

                    Card(
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.size(120.dp)
                    ) {
                        if (isUploadingFoto) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFFA000))
                            }
                        } else if (!fotoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(fotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.user),
                                error = painterResource(id = R.drawable.user)
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray)
                                    .padding(24.dp),
                                tint = Color.White
                            )
                        }
                    }

                    // Botão de editar foto
                    FloatingActionButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.BottomEnd),
                        containerColor = Color(0xFFFFA000)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar foto",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Barra inferior
            BarraTarefas(navController = navController)
        }

        // Snackbar
        if (showSnackbar) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
                action = {
                    TextButton(onClick = { showSnackbar = false }) {
                        Text("OK")
                    }
                }
            ) {
                Text(snackbarMessage)
            }
        }
    }
}

// ============================================
// COMPONENTES AUXILIARES
// ============================================

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFFFA000) else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.DarkGray
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(text = text, fontSize = 13.sp)
    }
}

@Composable
fun InformacoesTab(usuario: Usuario?, crianca: Crianca?, isCrianca: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (isCrianca) {
            InfoRow(label = "Nome", value = crianca?.nome ?: "")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Email", value = crianca?.email ?: "")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Tipo de conta", value = "Criança")
        } else {
            InfoRow(label = "Nome", value = usuario?.nome ?: "")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Email", value = usuario?.email ?: "")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Telefone", value = usuario?.telefone ?: "Não informado")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Tipo de conta", value = "Responsável")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.Black
        )
    }
}

@Composable
fun FilhosTab(filhos: List<Crianca>, navController: NavHostController?) {
    if (filhos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.LightGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Nenhum filho cadastrado",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filhos) { filho ->
                FilhoCard(filho = filho)
            }
        }
    }
}

@Composable
fun FilhoCard(filho: Crianca) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Foto
            if (!filho.foto_perfil.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(filho.foto_perfil)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de ${filho.nome}",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.user),
                    error = painterResource(id = R.drawable.user)
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = filho.nome,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ResponsaveisTab() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Responsáveis não disponíveis",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Funcionalidade em desenvolvimento",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

// ============================================
// FUNÇÃO DE UPLOAD
// ============================================

suspend fun uploadFotoPerfil(
    context: android.content.Context,
    uri: Uri,
    isCrianca: Boolean,
    usuario: Usuario?,
    crianca: Crianca?,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        Log.d(TAG, "📸 Upload de foto solicitado...")
        Log.d(TAG, "⚠️ Funcionalidade de upload de foto ainda não implementada")
        Log.d(TAG, "💡 Necessário implementar:")
        Log.d(TAG, "   1. AzureBlobService para upload")
        Log.d(TAG, "   2. Método atualizarFoto no CriancaService")
        Log.d(TAG, "   3. Método atualizarFoto no UsuarioService")

        // Por enquanto, apenas informa que não está disponível
        onError("Upload de foto ainda não disponível. Aguarde próxima atualização.")

    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro", e)
        onError("Erro: ${e.message}")
    }
}
