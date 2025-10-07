
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraTarefas() {
    // Degradê suave horizontal: cor original à esquerda para mais clara à direita
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),  // Laranja original
            Color(0xFFFFD27A)   // Laranja claro suave
        )
    )

    NavigationBar(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .background(gradient), // Aplica o degradê
        containerColor = Color.Transparent // Mantém transparente para o gradiente aparecer
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF5A3E1B) // cor escura do ícone
                    )
                    Text("Início", fontSize = 10.sp, color = Color(0xFF5A3E1B))
                }
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Atividades",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF5A3E1B)
                    )
                    Text("Atividades", fontSize = 10.sp, color = Color(0xFF5A3E1B))
                }
            }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Face,
                        contentDescription = "Perfil",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF5A3E1B)
                    )
                    Text("Perfil", fontSize = 10.sp, color = Color(0xFF5A3E1B))
                }
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 70)
@Composable
fun BarraTarefasPreview() {
    BarraTarefas()
}
