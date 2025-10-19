package com.oportunyfam_mobile.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.SearchBar
import com.oportunyfam_mobile.R

@Composable
fun HomeScreen(navController: NavHostController?) {

    var query by rememberSaveable { mutableStateOf("") }
    val searchResults = listOf("Jiu Jitsu", "Centro Cultural", "Biblioteca", "T.I")

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.mapa),
            contentDescription = "Mapa",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                // Futura chamada à API
                println("Pesquisa de API simulada: $it")
            },
            onSearchIconClick = {
                // Futura navegação para a tela de resultados
                println("Botão de Lupa clicado. Navegar para tela de resultados.")
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ChipMock("Jiu Jitsu")
            ChipMock("T.I")
            ChipMock("Centro Cultural")
            ChipMock("Biblioteca")
        }

        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(70.dp, 80.dp, 16.dp, 90.dp),
            containerColor = Color.DarkGray
        ) {
            Icon(Icons.Filled.Face, contentDescription = "Usuários")
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BarraTarefas(navController = navController)
        }
    }
}


@Composable
fun ChipMock(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.DarkGray)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = null)
}