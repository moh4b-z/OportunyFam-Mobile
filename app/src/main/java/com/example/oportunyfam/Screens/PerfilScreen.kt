package com.example.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.Components.BarraTarefas
import com.example.oportunyfam.R

@Composable
fun PerfilScreen(navController: NavHostController?) {

    var selectedOption by remember { mutableStateOf<String?>(null) }

    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),
            Color(0xFFFFD27A)
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
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificações", tint = Color.Black)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 65.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Localização
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "",
                            tint = Color.Gray,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Osasco-SP", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Horário
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "",
                            tint = Color.Gray,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("08:00 - 18:00", color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card de opções
                    Column(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth()
                            .background(Color(0xFFFFD580), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val buttons = listOf("Sobre nós", "Faça parte", "Associados")
                            buttons.forEach { label ->
                                Button(
                                    onClick = { selectedOption = label },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFB74D)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Mostra o card de descrição apenas se uma opção foi selecionada
                        selectedOption?.let { option ->
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    when (option) {
                                        "Sobre nós" -> Text(
                                            "A ONG promove a inclusão social e desenvolvimento de jovens por meio do esporte.",
                                            color = Color.Black
                                        )
                                        "Faça parte" -> CheckboxSingleOptionExample()
                                        "Associados" -> Text(
                                            "Informações sobre nossos associados e como se tornar um.",
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Divider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 120.dp)
                )
            }

            // Texto acima da foto
            Text(
                text = "127\nFOLLOWING",
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 5.dp),
                textAlign = TextAlign.Center
            )

            // Foto de perfil
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 60.dp)
                    .border(3.dp, Color.White, CircleShape),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "foto perfil",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        BarraTarefas(navController = navController)
    }
}

@Composable
fun CheckboxSingleOptionExample() {
    var oportunidades = remember { mutableStateListOf<Oportunidade>() }
    var selectedIndex by remember { mutableStateOf(-1) }

    // Carregar dados da API
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api.getOportunidades()
            oportunidades.addAll(response)
        } catch (e: Exception) {
            // Tratar erro (ex: Toast)
        }
    }

    Column {
        oportunidades.forEachIndexed { index, opcao ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${opcao.nome} (${opcao.vagas} vagas)", modifier = Modifier.weight(1f))
                RadioButton(
                    selected = selectedIndex == index,
                    onClick = {
                        if (opcao.vagas > 0) {
                            // Atualiza localmente a quantidade
                            oportunidades[index] = opcao.copy(vagas = opcao.vagas - 1)
                            selectedIndex = index

                            // Atualiza no servidor (chamada assíncrona)
                            LaunchedEffect(opcao.id) {
                                try {
                                    RetrofitClient.api.selecionarOportunidade(opcao.id)
                                } catch (e: Exception) {
                                    // Tratar erro
                                }
                            }
                        }
                    }
                )
            }
        }

        // Texto da opção selecionada
        if (selectedIndex != -1) {
            Text(
                text = "Selecionado: ${oportunidades[selectedIndex].nome}",
                color = Color.Gray
            )
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    PerfilScreen(navController = null)
}
