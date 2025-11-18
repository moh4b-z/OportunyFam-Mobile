package com.oportunyfam_mobile.Screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.Instituicao
import kotlinx.coroutines.launch

private const val TAG = "SearchResultsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    navController: NavHostController?,
    initialQuery: String = ""
) {
    val scope = rememberCoroutineScope()

    // Estados
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var instituicoes by remember { mutableStateOf<List<Instituicao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Função de busca
    fun buscarInstituicoes(query: String) {
        if (query.isBlank()) {
            errorMessage = "Digite algo para buscar"
            return
        }

        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                Log.d(TAG, "🔍 Buscando instituições: '$query'")
                val response = RetrofitFactory().getInstituicaoService().listarTodasSuspend()

                if (response.isSuccessful) {
                    val todasInstituicoes = response.body()?.instituicoes ?: emptyList()

                    // Filtrar localmente por nome
                    instituicoes = todasInstituicoes.filter {
                        it.nome.contains(query, ignoreCase = true)
                    }

                    Log.d(TAG, "✅ ${instituicoes.size} instituições encontradas")

                    if (instituicoes.isEmpty()) {
                        errorMessage = "Nenhuma instituição encontrada para '$query'"
                    }
                } else {
                    errorMessage = "Erro ao buscar: ${response.code()}"
                    Log.e(TAG, "❌ Erro na busca: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Erro: ${e.message}"
                Log.e(TAG, "❌ Erro na busca", e)
            } finally {
                isLoading = false
            }
        }
    }

    // Buscar automaticamente se já tiver query inicial
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            buscarInstituicoes(initialQuery)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Instituições") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFA000),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BarraTarefas(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de pesquisa
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Digite o nome da instituição...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFA000),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { buscarInstituicoes(searchQuery) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFFFFA000)
                        )
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.White
                        )
                    }
                }
            }

            // Conteúdo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    isLoading -> {
                        // Loading
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFFFFA000))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Buscando instituições...")
                            }
                        }
                    }
                    errorMessage != null -> {
                        // Erro ou vazio
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    instituicoes.isNotEmpty() -> {
                        // Grid de instituições
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(instituicoes) { instituicao ->
                                InstituicaoCard(
                                    instituicao = instituicao,
                                    onClick = {
                                        Log.d(TAG, "🖱️ Clicou em: ${instituicao.nome}")
                                        navController?.navigate("instituicao_perfil/${instituicao.instituicao_id}")
                                    }
                                )
                            }
                        }
                    }
                    else -> {
                        // Estado inicial
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Digite o nome de uma instituição para buscar",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstituicaoCard(
    instituicao: Instituicao,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Foto da instituição
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (!instituicao.foto_perfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(instituicao.foto_perfil)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de ${instituicao.nome}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.perfil),
                        error = painterResource(id = R.drawable.perfil)
                    )
                } else {
                    // Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.perfil),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray
                        )
                    }
                }
            }

            // Informações
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Nome
                Text(
                    text = instituicao.nome,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Localização
                if (instituicao.endereco != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFFA000)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${instituicao.endereco.cidade}, ${instituicao.endereco.estado}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

