package com.oportunyfam_mobile_ong.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.oportunyfam_mobile_ong.model.Mensagem
import com.oportunyfam_mobile_ong.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    conversaId: Int,
    nomeContato: String,
    pessoaIdAtual: Int,
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val mensagens by viewModel.mensagens.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var currentMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Carregar mensagens e iniciar escuta em tempo real do Firebase
    LaunchedEffect(conversaId) {
        viewModel.iniciarEscutaMensagens(conversaId)
    }

    // Scroll automático para a última mensagem
    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(mensagens.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                nomeContato = nomeContato,
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mensagens de erro
            errorMessage?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.limparErro() }) {
                            Text("OK", color = Color(0xFFD32F2F))
                        }
                    }
                }
            }

            // Lista de mensagens
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading && mensagens.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFF6F00))
                        }
                    }
                    mensagens.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma mensagem ainda\nSeja o primeiro a enviar!",
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        // Agrupar mensagens por data e manter ordem cronológica
                        val mensagensAgrupadas = mensagens
                            .sortedBy { it.criado_em } // Ordena do mais antigo para o mais recente
                            .groupBy { extrairData(it.criado_em) }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mensagensAgrupadas.forEach { (data, mensagensDoDia) ->
                                // Separador de data
                                item(key = "date_$data") {
                                    DateSeparator(data = data)
                                }

                                // Mensagens do dia (já ordenadas)
                                items(mensagensDoDia, key = { it.id }) { mensagem ->
                                    ChatMessage(
                                        mensagem = mensagem,
                                        isUser = mensagem.id_pessoa == pessoaIdAtual
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Campo de entrada
            ChatInputField(
                currentMessage = currentMessage,
                onMessageChange = { currentMessage = it },
                onSendClick = {
                    if (currentMessage.isNotBlank()) {
                        viewModel.enviarMensagem(conversaId, pessoaIdAtual, currentMessage)
                        currentMessage = ""
                    }
                },
                enabled = !isLoading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(nomeContato: String, onBackClick: () -> Unit) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFA726), Color(0xFFF57C00))
    )

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Text(
                        text = nomeContato.firstOrNull()?.uppercase() ?: "?",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6F00)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = nomeContato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Online",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White
        ),
        modifier = Modifier.background(gradient)
    )
}

@Composable
fun ChatMessage(mensagem: Mensagem, isUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFFDCF8C6) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = mensagem.descricao,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatarHora(mensagem.criado_em),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (mensagem.visto) "✓✓" else "✓",
                            fontSize = 11.sp,
                            color = if (mensagem.visto) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputField(
    currentMessage: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = currentMessage,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite uma mensagem...", fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF6F00),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                maxLines = 4,
                enabled = enabled
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier.size(48.dp),
                containerColor = Color(0xFFFF6F00),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun DateSeparator(data: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFFE0E0E0),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp
        ) {
            Text(
                text = formatarDataSeparador(data),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )
        }
    }
}

private fun extrairData(dataHora: String): String {
    return try {
        // Formato: "2025-11-08T21:45:33.000Z"
        dataHora.substring(0, 10) // Retorna "2025-11-08"
    } catch (e: Exception) {
        "Hoje"
    }
}

private fun formatarDataSeparador(data: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(data) ?: return data

        val hoje = Date()
        val ontem = Date(hoje.time - 24 * 60 * 60 * 1000)

        val dataFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        val hojeFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(hoje)
        val ontemFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ontem)

        when (dataFormatada) {
            hojeFormatada -> "Hoje"
            ontemFormatada -> "Ontem"
            else -> SimpleDateFormat("dd 'de' MMMM", Locale("pt", "BR")).format(date)
        }
    } catch (e: Exception) {
        data
    }
}

private fun formatarHora(dataHora: String): String {
    return try {
        val partes = dataHora.split("T")
        if (partes.size > 1) {
            partes[1].substring(0, 5)
        } else {
            "Agora"
        }
    } catch (e: Exception) {
        "Agora"
    }
}

@Preview(showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen(
        navController = rememberNavController(),
        conversaId = 1,
        nomeContato = "Laura de Andrade",
        pessoaIdAtual = 1
    )
}