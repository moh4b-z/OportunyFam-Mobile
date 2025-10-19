package com.oportunyfam_mobile.Screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.R



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavHostController?,
    instituicaoNome: String, // Dado dinâmico: Nome da Instituição
    instituicaoEmail: String, // Dado dinâmico: Email da Instituição
    onLogout: () -> Unit // Função para lidar com o Logout
) {

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
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            // Botão de Logout que chama a função de limpeza de dados e navegação
            IconButton(onClick = { onLogout() }) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Sair", tint = Color.Black)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificações", tint = Color.Black)
            }
            IconButton(onClick = {  }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black)
            }
        }

        Divider(color = Color.LightGray, thickness = 1.5.dp)

        // Conteúdo principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Card branco
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
                        .padding(top = 90.dp) // Espaço para a imagem
                ) {
                    // Informações do perfil
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Nome da Instituição (DADO DINÂMICO)
                        Text(
                            instituicaoNome,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Email da Instituição (DADO DINÂMICO)
                        Text(
                            instituicaoEmail,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )


                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats (FOLLOWING)
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
                                modifier = Modifier.weight(1f) // ocupa o espaço restante
                            )
                        }


                        Spacer(modifier = Modifier.height(32.dp))

                        // Divisor
                        Divider(
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

        BarraTarefas(navController = navController)
    }
}

@Preview(showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    PerfilScreen(
        navController = null,
        instituicaoNome = "Mohammad ONG (Preview)",
        instituicaoEmail = "mohammadsalim10000@gmail.com",
        onLogout = {}
    )
}
