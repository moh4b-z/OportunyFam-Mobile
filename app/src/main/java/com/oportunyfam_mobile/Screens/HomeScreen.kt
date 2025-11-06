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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.SearchBar
import com.oportunyfam_mobile.Service.RetrofitClient
import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.model.InstituicaoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun HomeScreen(navController: NavHostController?) {

    // === Estados ===
    var query by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Instituicao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    // Função de busca
    fun buscarInstituicoes(termo: String) {
        if (termo.isBlank()) return
        isLoading = true
        hasSearched = true

        RetrofitClient.instituicaoService.buscarComFiltro(termo, 1, 20)
            .enqueue(object : Callback<List<InstituicaoResponse>> {
                override fun onResponse(
                    call: Call<List<InstituicaoResponse>>,
                    response: Response<List<InstituicaoResponse>>
                ) {
                    isLoading = false
                    searchResults = if (response.isSuccessful) {
                        response.body()?.mapNotNull { it.instituicao } ?: emptyList()
                    } else emptyList()
                }

                override fun onFailure(call: Call<List<InstituicaoResponse>>, t: Throwable) {
                    isLoading = false
                    t.printStackTrace()
                    searchResults = emptyList()
                }
            })
    }

    // limpa resultados quando query ficar vazia
    LaunchedEffect(query) {
        if (query.isBlank()) {
            searchResults = emptyList()
            hasSearched = false
        }
    }

    // === Mapa ===
    val initialLatLng = LatLng(-23.5505, -46.6333)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 12f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ===== Mapa de fundo =====
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        )

        // ===== Barra de pesquisa =====
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { buscarInstituicoes(it) },
            onSearchIconClick = { buscarInstituicoes(query) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        // ===== Resultados =====
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
                                    // Exemplo: navegação futura
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

        // ===== Indicador de carregamento =====
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

        // ===== Botão flutuante =====
        FloatingActionButton(
            onClick = { /* ação futura */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF424242)
        ) {
            Icon(Icons.Filled.Face, contentDescription = "Usuários", tint = Color.White)
        }

        // ===== Barra inferior =====
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BarraTarefas(navController = navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = null)
}
