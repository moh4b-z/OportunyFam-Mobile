package com.oportunyfam_mobile.Screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.MapViewGoogle
import com.oportunyfam_mobile.Components.SearchBar
import com.oportunyfam_mobile.Service.InstituicaoService
import com.oportunyfam_mobile.Service.RetrofitClient
import com.oportunyfam_mobile.model.Instituicao
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun HomeScreen(navController: NavHostController?) {

    // Estado da barra de pesquisa
    var query by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Instituicao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Retrofit service
    val instituicaoService = RetrofitClient.instance.create(InstituicaoService::class.java)

    fun buscarInstituicoes(termo: String) {
        if (termo.isBlank()) return
        isLoading = true

        val call = instituicaoService.buscarComFiltro(termo, 1, 20)
        call.enqueue(object : Callback<List<Instituicao>> {
            override fun onResponse(
                call: Call<List<Instituicao>>,
                response: Response<List<Instituicao>>
            ) {
                isLoading = false
                searchResults = if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else emptyList()
            }

            override fun onFailure(call: Call<List<Instituicao>>, t: Throwable) {
                isLoading = false
                t.printStackTrace()
                searchResults = emptyList()
            }
        })
    }

    // Localização padrão (São Paulo)
    val initialLat = -23.5505
    val initialLon = -46.6333

    // Zoom padrão
    val initialZoom = 10

    Box(modifier = Modifier.fillMaxSize()) {
        // === Google Maps ===
        MapViewGoogle(
            modifier = Modifier.fillMaxSize(),
            initialLat = initialLat,
            initialLon = initialLon,
            initialZoom = initialZoom.toFloat(),
            markers = searchResults
        )

        // === Barra de pesquisa ===
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { buscarInstituicoes(it) },
            onSearchIconClick = { buscarInstituicoes(query) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        // === Resultados da busca ===
        if (searchResults.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(top = 90.dp)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .verticalScroll(rememberScrollState())
                ) {
                    searchResults.forEach { ong ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // navController?.navigate("detalhesOng/${ong.id}")
                                }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = ong.nome ?: "Sem nome",
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                    }
                }
            }
        }

        // === Indicador de carregamento ===
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 90.dp)
                )
            }

            searchResults.isEmpty() && query.isNotBlank() -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 90.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma ONG encontrada.", color = Color.Gray)
                }
            }
        }

        // === Barra de tarefas inferior ===
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BarraTarefas(navController = navController)
        }

        // === Botão flutuante ===
        FloatingActionButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color.DarkGray
        ) {
            Icon(Icons.Filled.Face, contentDescription = "Usuários", tint = Color.White)
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
