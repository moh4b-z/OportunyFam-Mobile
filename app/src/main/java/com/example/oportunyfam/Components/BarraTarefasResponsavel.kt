package com.example.oportunyfam.Components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraTarefas() {
    NavigationBar(
        containerColor = Color(0xFFFFA000),
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                    Text("Início", fontSize = 10.sp, color = Color.White)
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
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                    Text("Atividades", fontSize = 10.sp, color = Color.White)
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
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                    Text("Perfil", fontSize = 10.sp, color = Color.White)
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