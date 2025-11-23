package com.oportunyfam_mobile.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun BarraTarefas(
    navController: NavHostController? = null,
    currentRoute: String = ""
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),
            Color(0xFFFFD27A)
        )
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .navigationBarsPadding() // respeita a área dos botões do sistema
            .height(64.dp),
        containerColor = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        // Item Início
        NavigationBarItem(
            selected = currentRoute == "HomeScreen",
            onClick = { navController?.navigate("HomeScreen") },
            icon = {
                NavItemContent(
                    icon = Icons.Filled.Home,
                    label = "Início",
                    isSelected = currentRoute == "HomeScreen"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color(0xFF5A3E1B),
                unselectedTextColor = Color(0xFF5A3E1B)
            )
        )

        // Item Conversas
        NavigationBarItem(
            selected = currentRoute == "ConversasScreen",
            onClick = { navController?.navigate("ConversasScreen") },
            icon = {
                NavItemContent(
                    icon = Icons.Filled.Email,
                    label = "Conversas",
                    isSelected = currentRoute == "ConversasScreen"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color(0xFF5A3E1B),
                unselectedTextColor = Color(0xFF5A3E1B)
            )
        )

        // Item Perfil
        NavigationBarItem(
            selected = currentRoute == "tela_perfil",
            onClick = { navController?.navigate("tela_perfil") },
            icon = {
                NavItemContent(
                    icon = Icons.Filled.Person,
                    label = "Perfil",
                    isSelected = currentRoute == "tela_perfil"
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color.White,
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color(0xFF5A3E1B),
                unselectedTextColor = Color(0xFF5A3E1B)
            )
        )
    }
}

@Composable
private fun NavItemContent(
    icon: ImageVector,
    label: String,
    isSelected: Boolean
) {
    Column(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .height(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = if (isSelected) Color.White else Color(0xFF5A3E1B)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 88)
@Composable
fun BarraTarefasPreview() {
    BarraTarefas(currentRoute = "HomeScreen")
}