package com.oportunyfam_mobile.Screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.EditarPerfilDialog
import com.oportunyfam_mobile.Components.EditChildDialog
import com.oportunyfam_mobile.Components.PerfilTopBar
import com.oportunyfam_mobile.Components.PerfilPhoto
import com.oportunyfam_mobile.Components.PerfilTabs
import com.oportunyfam_mobile.Components.ChildDetailDialog
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.data.AuthType
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "PerfilScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavHostController?) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val authDataStore = remember { AuthDataStore(context) }

    // Estados principais
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var crianca by remember { mutableStateOf<Crianca?>(null) }
    var isCrianca by remember { mutableStateOf(false) }
    var criancas by remember { mutableStateOf<List<Crianca>>(emptyList()) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    // Estados de UI
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Informações") }
    // State to show child full-detail dialog
    var showChildDetail by remember { mutableStateOf(false) }

    // Handlers para salvar após editar (declarados aqui para evitar warnings de escopo)
    val handleChildSave: (Crianca) -> Unit = { updatedChild ->
        crianca = updatedChild
        showEditDialog = false
        snackbarMessage = "Perfil atualizado"
        showSnackbar = true
    }

    val handleUserSave: (Usuario) -> Unit = { updatedUsuario ->
        usuario = updatedUsuario
        showEditDialog = false
        snackbarMessage = "Perfil atualizado"
        showSnackbar = true
    }

    // Handler when a child mini card is selected: fetch full details and open detail dialog
    val onChildSelected: (Crianca) -> Unit = { mini ->
        scope.launch {
            try {
                Log.d(TAG, "🔎 Carregando detalhes da criança id=${mini.crianca_id}...")
                val resp = withContext(Dispatchers.IO) {
                    RetrofitFactory().getCriancaService().buscarPorId(mini.crianca_id).execute()
                }
                if (resp.isSuccessful) {
                    val full = resp.body()?.crianca
                    if (full != null) {
                        Log.d(TAG, "✅ Criança carregada: id=${full.crianca_id}, foto=${full.foto_perfil}")
                        crianca = full
                        showEditDialog = false
                        showChildDetail = true
                    }
                } else {
                    Log.e(TAG, "Erro ao carregar criança completa: ${resp.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exceção ao carregar criança: ${e.message}", e)
            }
        }
    }

    // Carregar dados do perfil
    LaunchedEffect(reloadTrigger) {
        Log.d(TAG, "🔄 Carregando perfil (trigger=$reloadTrigger)...")
        val authData = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }

        if (authData != null) {
            Log.d(TAG, "✅ Tipo de usuário: ${authData.type}")

            when (authData.type) {
                AuthType.USUARIO -> {
                    isCrianca = false
                    // start from what's in the auth store, but refresh from backend to get full data
                    val authUsuario = authData.user as? Usuario
                    usuario = authUsuario
                    Log.d(TAG, "👤 Usuário carregado (do auth store): ${usuario?.nome}")

                    // Buscar filhos via endpoint de usuário (buscarPorId) — executar em IO
                    val usuarioId = authUsuario?.usuario_id
                    if (usuarioId != null) {
                        try {
                            val resp = withContext(Dispatchers.IO) {
                                RetrofitFactory().getUsuarioService().buscarPorId(usuarioId).execute()
                            }
                            if (resp.isSuccessful) {
                                val usuarioResp = resp.body()?.usuario
                                // Atualiza estado na main thread (já estamos no LaunchedEffect)
                                usuario = usuarioResp
                                val miniList = usuarioResp?.criancas_dependentes ?: emptyList()
                                criancas = miniList.map { mini ->
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
                                Log.d(TAG, "👶 Crianças carregadas (via usuario): ${criancas.size}")
                            } else {
                                Log.e(TAG, "Erro ao buscar usuario para crianças: ${resp.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Erro ao buscar crianças", e)
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
                popUpTo(0
                )
            }
        }
    }

    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFA000), Color(0xFFFFD27A))
    )

    // === UI ===
    Box(modifier = Modifier.fillMaxSize()) {
        // Background + top column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient)
                .statusBarsPadding()
                .padding(top = 8.dp) // pequeno espaçamento
        ) {
            // Top bar
            PerfilTopBar(navController = navController, onEdit = { showEditDialog = true }, onLogout = { onLogout() })

            // Conteúdo principal: card com foto e tabs
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
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
                        // Header: nome, email e tabs
                        item {
                            Text(
                                text = if (isCrianca) crianca?.nome ?: "Criança" else usuario?.nome ?: "Usuário",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isCrianca) crianca?.email ?: "" else usuario?.email ?: "",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))

                            // Tabs
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                TabButton(text = "Informações", isSelected = selectedTab == "Informações", onClick = { selectedTab = "Informações" })
                                TabButton(text = if (isCrianca) "Responsáveis" else "Crianças", isSelected = selectedTab == if (isCrianca) "Responsáveis" else "Crianças", onClick = { selectedTab = if (isCrianca) "Responsáveis" else "Crianças" })
                            }

                            Spacer(Modifier.height(24.dp))
                        }

                        // Conteúdo da aba selecionada
                        item {
                            when (selectedTab) {
                                "Informações" -> InformacoesTab(usuario = usuario, crianca = crianca, isCrianca = isCrianca)
                                "Crianças" -> PerfilTabs(
                                    selectedTab = "Crianças",
                                    criancas = criancas,
                                    onChildClick = onChildSelected,
                                    onChildDelete = { mini ->
                                        // Delete handler for child mini: call API and remove from list on success
                                        scope.launch {
                                            try {
                                                val delResp = withContext(Dispatchers.IO) {
                                                    RetrofitFactory().getCriancaService().deletar(mini.crianca_id).execute()
                                                }
                                                if (delResp.isSuccessful) {
                                                    // remove local
                                                    criancas = criancas.filterNot { it.crianca_id == mini.crianca_id }
                                                    Log.d(TAG, "Criança deletada: ${mini.crianca_id}")
                                                } else {
                                                    Log.e(TAG, "Falha ao deletar criança: ${delResp.code()}")
                                                }
                                            } catch (e: Exception) {
                                                Log.e(TAG, "Erro ao deletar criança: ${e.message}", e)
                                            }
                                        }
                                    }
                                )
                                "Responsáveis" -> PerfilTabs(
                                    selectedTab = "Responsáveis",
                                    responsaveis = crianca?.responsaveis ?: emptyList(),
                                    onResponsavelClick = { responsavel ->
                                        // TODO: Implementar navegação para perfil do responsável ou abrir diálogo
                                        Log.d(TAG, "Clicou no responsável: ${responsavel.nome}")
                                    }
                                )
                             }
                         }
                     }
                }

                // Foto de perfil sobreposta (filha do Box) — esta chamada está dentro do BoxScope, então Modifier.align funciona
                Box(modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                ) {
                    PerfilPhoto(usuario = usuario, crianca = crianca, isUploading = false)
                }
            }

            // Barra inferior fixa dentro da Column (abaixo do conteúdo principal)
            Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                BarraTarefas(navController = navController)
            }
        }

        // Snackbar sobreposto ao resto da UI
        if (showSnackbar) {
            Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp), action = {
                TextButton(onClick = { showSnackbar = false }) { Text("OK") }
            }) { Text(snackbarMessage) }
        }
    }

    // Dialogs (fora do Column/Box)
    if (showEditDialog) {
        if (isCrianca) {
            EditChildDialog(crianca = crianca, onDismiss = { showEditDialog = false }, onSave = handleChildSave)
        } else if (usuario != null) {
            EditarPerfilDialog(usuario = usuario!!, onDismiss = { showEditDialog = false }, onSave = handleUserSave, usuarioService = RetrofitFactory().getUsuarioService(), scope = scope)
        }
    }

    if (showChildDetail) {
        ChildDetailDialog(
            child = crianca,
            onDismiss = { showChildDetail = false },
            onStartConversation = {
                // Create conversation with this child and navigate to chat
                scope.launch {
                    try {
                        val pessoaAtual = when {
                            usuario != null -> usuario!!.pessoa_id
                            crianca != null -> crianca!!.pessoa_id
                            else -> 0
                        }

                        val otherPessoaId = crianca?.pessoa_id ?: 0

                        val participantes = listOf(pessoaAtual, otherPessoaId)
                        val resp = withContext(Dispatchers.IO) {
                            RetrofitFactory().getConversaService().criar(com.oportunyfam_mobile.model.ConversaRequest(participantes))
                        }
                        if (resp.isSuccessful) {
                            val conversa = resp.body()?.conversa
                            val conversaId = conversa?.id ?: resp.code() // fallback
                            val nomeContato = java.net.URLEncoder.encode(crianca?.nome ?: "", "UTF-8")
                            val pId = pessoaAtual
                            navController?.navigate("${com.oportunyfam_mobile.MainActivity.CHAT}/$conversaId/$nomeContato/$pId")
                            showChildDetail = false
                        } else {
                            Log.e(TAG, "Erro ao criar/abrir conversa: ${resp.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exceção criar conversa: ${e.message}", e)
                    }
                }
            },
            onChildUpdated = { updatedChild: Crianca ->
                // Atualizar a criança na lista e fechar o diálogo
                crianca = updatedChild
                criancas = criancas.map { criancaItem ->
                    if (criancaItem.crianca_id == updatedChild.crianca_id) updatedChild else criancaItem
                }
                showChildDetail = false
                snackbarMessage = "Criança atualizada com sucesso"
                showSnackbar = true
                // Recarregar dados
                reloadTrigger++
            }
        )
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
            InfoRow("Nome", usuario?.nome ?: "")
            Spacer(Modifier.height(12.dp))
            InfoRow("Email", usuario?.email ?: "")
            Spacer(Modifier.height(12.dp))
            InfoRow("Telefone", usuario?.telefone ?: "Não informado")
            Spacer(Modifier.height(12.dp))
            InfoRow("Tipo de conta", "Responsável")
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
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.Black
        )
    }
}
