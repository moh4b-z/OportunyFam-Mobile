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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState // <--- IMPORT ADICIONADO
import com.google.maps.android.compose.rememberCameraPositionState
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.SearchBar
import com.oportunyfam_mobile.Service.InstituicaoService
import com.oportunyfam_mobile.Service.RetrofitClient
import com.oportunyfam_mobile.model.Instituicao
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


@Composable
fun HomeScreen(navController: NavHostController?) {

    // --- Estado da barra de pesquisa ---
    var query by rememberSaveable { mutableStateOf("") }

    // --- Lista com os resultados de busca ---
    var searchResults by remember { mutableStateOf<List<Instituicao>>(emptyList()) }

    // --- Estado de carregamento (exibe indicador enquanto busca) ---
    var isLoading by remember { mutableStateOf(false) }

    // --- Cria o serviço Retrofit para chamar a API ---
    val instituicaoService = RetrofitClient.instance.create(InstituicaoService::class.java)

    // --- Função para buscar instituições ---
    fun buscarInstituicoes(termo: String) {
        if (termo.isBlank()) return // evita chamada vazia

        isLoading = true // mostra o carregamento

        val call = instituicaoService.buscarComFiltro(termo, 1, 20)
        call.enqueue(object : Callback<List<Instituicao>> {
            override fun onResponse(
                call: Call<List<Instituicao>>,
                response: Response<List<Instituicao>>
            ) {
                isLoading = false
                if (response.isSuccessful) {
                    // Atualiza a lista com os resultados vindos do servidor
                    searchResults = response.body() ?: emptyList()
                } else {
                    // Se a resposta falhar, limpa os resultados
                    searchResults = emptyList()
                }
            }

            override fun onFailure(call: Call<List<Instituicao>>, t: Throwable) {
                isLoading = false
                t.printStackTrace()
                searchResults = emptyList()
            }
        })
    }

    // --- Posição inicial do mapa ---
    val defaultLocation = LatLng(-23.5505, -46.6333) // São Paulo
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }


    // --- Layout da tela ---
    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // 🔹 Adiciona marcadores das instituições
            searchResults.forEach { ong ->
                ong.endereco?.let { endereco ->

                    // --- CORREÇÃO APLICADA AQUI ---
                    // 1. Criamos um 'MarkerState' para guardar a posição.
                    val markerState = MarkerState(position = LatLng(endereco.latitude, endereco.longitude))

                    // 2. Usamos 'state' em vez de 'position' e passamos o 'MarkerState'.
                    Marker(
                        state = markerState,
                        title = ong.nome ?: "Sem nome"
                    )
                    // --- FIM DA CORREÇÃO ---
                }
            }
        }

        // --- Barra de pesquisa no topo ---
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { buscarInstituicoes(it) },
            onSearchIconClick = { buscarInstituicoes(query) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        // --- Resultados da busca abaixo da barra ---
        if (searchResults.isNotEmpty()) { // Mostra a lista apenas se houver resultados
            Column(
                modifier = Modifier
                    .padding(top = 90.dp) // Ajuste para não sobrepor a searchbar
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp) // Limita a altura da lista
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .verticalScroll(rememberScrollState())
                ) {
                    searchResults.forEach { ong ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Navegação para detalhes (exemplo)
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

        // --- Indicador de carregamento ou mensagem de "nenhum resultado" ---
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 90.dp) // Centraliza abaixo da barra de busca
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
                ){
                    Text(
                        text = "Nenhuma ONG encontrada.",
                        color = Color.Gray
                    )
                }
            }
        }

        // --- Barra de tarefas inferior ---
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // Note que sua barra de tarefas pode cobrir o botão flutuante.
            // Ajuste os paddings conforme necessário.
            BarraTarefas(navController = navController)
        }

        // --- Botão flutuante ---
        FloatingActionButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp), // Padding mais simples
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
    // Para o preview funcionar, é melhor não passar um NavController real.
    HomeScreen(navController = null)
}
