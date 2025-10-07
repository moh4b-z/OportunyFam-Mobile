package com.example.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun BarraTarefas(navController: NavHostController?) {

    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),
            Color(0xFFFFD27A)
        )
    )

    // Controle do item selecionado
    var selectedItem by remember { mutableStateOf("home") }

    NavigationBar(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .background(gradient),
        containerColor = Color.Transparent
    ) {
        // Ícone Home
        NavigationBarItem(
            selected = selectedItem == "home",
            onClick = {
                selectedItem = "home"
                navController?.navigate("tela_home") {

                    if (navController != null) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                    launchSingleTop = true
                }
            },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Início",
                        modifier = Modifier.size(20.dp),
                        tint = if (selectedItem == "home") Color.White else Color(0xFF5A3E1B)
                    )
                    Text(
                        "Início",
                        fontSize = 10.sp,
                        color = if (selectedItem == "home") Color.White else Color(0xFF5A3E1B)
                    )
                }
            }
        )


        // Ícone Atividades
        NavigationBarItem(
            selected = selectedItem == "atividades",
            onClick = {
                selectedItem = "atividades"
            },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Atividades",
                        modifier = Modifier.size(20.dp),
                        tint = if (selectedItem == "atividades") Color.White else Color(0xFF5A3E1B)
                    )
                    Text(
                        "Atividades",
                        fontSize = 10.sp,
                        color = if (selectedItem == "atividades") Color.White else Color(0xFF5A3E1B)
                    )
                }
            }
        )

        // Ícone Perfil
        NavigationBarItem(
            selected = selectedItem == "perfil",
            onClick = {
                selectedItem = "perfil"
                navController?.navigate("tela_perfil") {

                    popUpTo("tela_perfil") { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Face,
                        contentDescription = "Perfil",
                        modifier = Modifier.size(20.dp),
                        tint = if (selectedItem == "perfil") Color.White else Color(0xFF5A3E1B)
                    )
                    Text(
                        "Perfil",
                        fontSize = 10.sp,
                        color = if (selectedItem == "perfil") Color.White else Color(0xFF5A3E1B)
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 70)
@Composable
fun BarraTarefasPreview() {
    BarraTarefas(navController = null)
}