package com.oportunyfam_mobile.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.SearchBar
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.Service.RetrofitClient
import com.oportunyfam_mobile.Service.InstituicaoService
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

    // --- Layout da tela ---
    Box(modifier = Modifier.fillMaxSize()) {

        // --- Fundo com imagem do mapa ---
        Image(
            painter = painterResource(id = R.drawable.mapa),
            contentDescription = "Mapa",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // --- Barra de pesquisa no topo ---
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = { buscarInstituicoes(it) }, // dispara busca ao apertar enter
            onSearchIconClick = { buscarInstituicoes(query) }, // dispara ao clicar na lupa
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )

        // --- Resultados da busca abaixo da barra ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.9f))
                .verticalScroll(rememberScrollState())
        ) {
            when {
                isLoading -> {
                    // Indicador de carregamento
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }

                searchResults.isNotEmpty() -> {
                    // Lista de resultados
                    searchResults.forEach { ong ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // 👇 Aqui você pode navegar para uma tela de detalhes
                                    // Exemplo: navController?.navigate("detalhesOng/${ong.id}")
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = ong.nome ?: "Sem nome",
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                        Divider()
                    }
                }

                query.isNotBlank() -> {
                    // Nenhum resultado encontrado
                    Text(
                        text = "Nenhuma ONG encontrada.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                }
            }
        }

        // --- Chips fixos (mantidos do seu layout original) ---
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

        // --- Botão flutuante ---
        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(70.dp, 80.dp, 16.dp, 90.dp),
            containerColor = Color.DarkGray
        ) {
            Icon(Icons.Filled.Face, contentDescription = "Usuários")
        }

        // --- Barra de tarefas inferior ---
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

// --- Preview da tela ---
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = null)
}
