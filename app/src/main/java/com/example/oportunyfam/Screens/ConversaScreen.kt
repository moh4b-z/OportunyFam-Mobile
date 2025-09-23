package com.example.tcc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oportunyfam.R

// Modelo da conversa
data class Conversa(
    val nome: String,
    val mensagem: String,
    val categoria: String,
    val icone: Int,
    val naoLidas: Int = 0,
    val online: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversaScreen() {
    val conversas = listOf(
        Conversa("Laura de Andrade", "Olá, fico feliz que tenha entrado...", "ONG", R.drawable.logo, 1, true),
        Conversa("Instituto Aprender", "Temos vagas disponíveis para de...", "Escola", R.drawable.logo, 2, true),
        Conversa("Escola Esperança", "As inscrições para o reforço escolar...", "Escola", R.drawable.logo)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFFA000),
                modifier = Modifier.height(60.dp)
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Conversas",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Perfil",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = Color.DarkGray
            ) {
                Icon(Icons.Filled.Person, contentDescription = "Usuários")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Topo com logo + título + notificações
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo), // Substitua pela sua logo
                    contentDescription = "Logo",
                    modifier = Modifier.size(50.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Conversas",
                    fontSize = 22.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificações",
                    tint = Color.Black
                )
            }

            // Barra de pesquisa
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Buscar conversas...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Divider(
                color = Color.LightGray,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de conversas
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversas) { conversa ->
                    ConversaCard(conversa)
                }
            }
        }
    }
}

@Composable
fun ConversaCard(conversa: Conversa) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto + status online
            Box {
                Image(
                    painter = painterResource(id = conversa.icone),
                    contentDescription = conversa.nome,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                if (conversa.online) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(conversa.nome, fontSize = 16.sp, color = Color.Black)
                Text(
                    conversa.mensagem,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                // Badge da categoria
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFA000))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(conversa.categoria, color = Color.White, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Contador de mensagens não lidas
                if (conversa.naoLidas > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.Green),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(conversa.naoLidas.toString(), color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversaScreenPreview() {
    ConversaScreen()
}