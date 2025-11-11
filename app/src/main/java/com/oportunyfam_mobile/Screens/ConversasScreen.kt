package com.oportunyfam_mobile.Screens

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.MainActivity
import com.oportunyfam_mobile.ViewModel.ChatViewModel
import com.oportunyfam_mobile.ViewModel.ConversaUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversasScreen(
    navController: NavHostController?
) {
    val context = LocalContext.current
    val viewModel = remember {
        ChatViewModel(context.applicationContext as android.app.Application)
    }

    val conversas by viewModel.conversas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val pessoaId by viewModel.pessoaId.collectAsState()

    // ✅ AGUARDA o ID da pessoa estar disponível antes de carregar conversas
    LaunchedEffect(pessoaId) {
        if (pessoaId != null) {
            viewModel.carregarConversas()
        }
    }

    Scaffold(
        topBar = { ConversasTopBarPremium() },
        bottomBar = { BarraTarefas(navController, "ConversasScreen") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Abrir diálogo para nova conversa */ },
                containerColor = Color(0xFFFF6F00),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nova conversa")
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading && conversas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF6F00))
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Erro desconhecido",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.carregarConversas() }) {
                            Text("Tentar novamente")
                        }
                    }
                }
                conversas.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.oportunyfam_mobile.R.drawable.logo),
                            contentDescription = "Sem conversas",
                            modifier = Modifier.size(100.dp),
                            colorFilter = ColorFilter.tint(Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma conversa ainda",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = "Suas conversas aparecerão aqui",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    ConversaListPremium(
                        conversas = conversas,
                        onConversaClick = { conversa ->
                            // ✅ Codifica o nome para evitar problemas com espaços e caracteres especiais
                            val nomeEncoded = Uri.encode(conversa.nome)
                            val pId = pessoaId ?: 0
                            navController?.navigate("${MainActivity.CHAT}/${conversa.id}/$nomeEncoded/$pId")
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversasTopBarPremium() {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFA726), Color(0xFFF57C00))
    )
    TopAppBar(
        title = { Text("Conversas", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White
        ),
        modifier = Modifier
            .background(gradient)
            .fillMaxWidth(),
        actions = {
            IconButton(onClick = { /* ação */ }) {
                Image(
                    painter = painterResource(id = com.oportunyfam_mobile.R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    )
}

@Composable
fun ConversaListPremium(
    conversas: List<ConversaUI>,
    onConversaClick: (ConversaUI) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(conversas) { conversa ->
            ConversaItemPremium(
                conversa = conversa,
                onClick = { onConversaClick(conversa) }
            )
        }
    }
}

@Composable
fun AvatarDegrade(imagem: Int, online: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_animation")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_offset"
    )

    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFA726), Color(0xFFF57C00), Color(0xFFFFA726)),
        start = Offset(animatedOffset, 0f),
        end = Offset(100f + animatedOffset, 100f)
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .border(width = 3.dp, brush = gradient, shape = CircleShape)
    ) {
        Image(
            painter = painterResource(id = imagem),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .align(Alignment.Center)
        )
        if (online) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun ConversaItemPremium(conversa: ConversaUI, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarDegrade(imagem = conversa.imagem, online = conversa.online)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(conversa.nome, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    conversa.ultimaMensagem,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(conversa.hora, color = Color(0xFF616161), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                if (conversa.mensagensNaoLidas > 0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(20.dp)
                            .background(Color(0xFFD32F2F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${conversa.mensagensNaoLidas}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConversasScreen() {
    ConversasScreen(null)
}