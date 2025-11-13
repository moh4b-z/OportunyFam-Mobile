package com.oportunyfam_mobile.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.EditarPerfilDialog
import com.oportunyfam_mobile.Components.PrimaryColor
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.data.AuthType
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    navController: NavHostController?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authDataStore = remember { AuthDataStore(context) }

    // Estados para armazenar as informações dinâmicas do usuário
    var instituicaoNome by remember { mutableStateOf("Carregando...") }
    var instituicaoEmail by remember { mutableStateOf("Carregando...") }
    var usuarioAtual by remember { mutableStateOf<com.oportunyfam_mobile.model.Usuario?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var usuarioService by remember { mutableStateOf<com.oportunyfam_mobile.Service.UsuarioService?>(null) }

    // Efeito que carrega os dados assim que a tela abre
    LaunchedEffect(Unit) {
        val authData = authDataStore.loadAuthUser()
        usuarioService = com.oportunyfam_mobile.Service.RetrofitFactory().getUsuarioService()

        if (authData != null) {
            when (authData.type) {
                AuthType.USUARIO -> {
                    val user = authData.user as? com.oportunyfam_mobile.model.Usuario
                    if (user != null) {
                        usuarioAtual = user
                        instituicaoNome = user.nome
                        instituicaoEmail = user.email
                    }
                }

                AuthType.CRIANCA -> {
                    val crianca = authData.user as? com.oportunyfam_mobile.model.Crianca
                    instituicaoNome = crianca?.nome ?: "Criança"
                    instituicaoEmail = crianca?.email ?: "Email não disponível"
                }
            }
        } else {
            instituicaoNome = "Usuário não logado"
            instituicaoEmail = ""
        }
    }

    // Função de logout
    fun onLogout() {
        coroutineScope.launch {
            authDataStore.logout()
            // Após limpar o estado de autenticação, volta para a tela de login
            navController?.navigate("login") {
                popUpTo(0) // Remove o histórico de navegação
            }
        }
    }

    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),  // Laranja original
            Color(0xFFFFD27A)   // Laranja claro suave
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "Voltar", tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { onLogout() }) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair", tint = Color.Black)
            }
            IconButton(onClick = {

            }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black)
            }
        }

        HorizontalDivider(color = Color.White, thickness = 1.5.dp)

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
                    .fillMaxHeight(0.75f)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 90.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Seção Nome, Email e Botão de Editar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Nome e e-mail dinâmicos
                                Text(
                                    instituicaoNome,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    instituicaoEmail,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            // Botão de Editar
                            if (usuarioAtual != null) {
                                IconButton(
                                    onClick = { showEditDialog = true },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar Perfil",
                                        tint = PrimaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Estatísticas fixas por enquanto
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "127",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "FOLLOWING",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Descrição
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Imagem Perfil da Instituição",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                "Está participando da ONG há 4 meses. A organização tem como objetivo implementar o esporte nas comunidades, promovendo inclusão social, desenvolvimento pessoal e mais oportunidades para crianças e jovens.",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Imagem de perfil centralizada
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset(y = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(70.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "Imagem Perfil Pessoal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(150.dp)
                    )
                }
            }
        }

        // Barra inferior (navegação)
        BarraTarefas(navController = navController)
    }

    // Dialog de editar perfil
    if (showEditDialog && usuarioAtual != null && usuarioService != null) {
        EditarPerfilDialog(
            usuario = usuarioAtual!!,
            onDismiss = { showEditDialog = false },
            onSave = { usuarioAtualizado ->
                usuarioAtual = usuarioAtualizado
                instituicaoNome = usuarioAtualizado.nome
                instituicaoEmail = usuarioAtualizado.email
                // Atualizar dados no DataStore
                coroutineScope.launch {
                    val authData = authDataStore.loadAuthUser()
                    if (authData != null) {
                        authDataStore.saveAuthUser(usuarioAtualizado, AuthType.USUARIO)
                    }
                }
            },
            usuarioService = usuarioService!!,
            scope = coroutineScope
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    PerfilScreen(navController = null)
}
