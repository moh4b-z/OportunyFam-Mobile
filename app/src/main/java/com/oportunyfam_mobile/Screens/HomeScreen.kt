package com.oportunyfam_mobile.Screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.SearchBar
import com.oportunyfam_mobile.Components.CategoryFilterRow
import com.oportunyfam_mobile.Components.Category
import com.oportunyfam_mobile.Service.LocationManager
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.Service.PlacesService
import com.oportunyfam_mobile.Service.PlaceInstituicao
import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.model.InstituicaoListResponse
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(navController: NavHostController?) {
    // === Contexto ===
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // === Estados ===
    var query by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Instituicao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Estados de instituições cadastradas e não cadastradas
    var instituicoesCadastradas by remember { mutableStateOf<List<Instituicao>>(emptyList()) }
    var instituicoesNaoCadastradas by remember { mutableStateOf<List<PlaceInstituicao>>(emptyList()) }
    var isLoadingInstituicoes by remember { mutableStateOf(false) }

    // Estados de localização
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationManager by remember { mutableStateOf<LocationManager?>(null) }
    var placesService by remember { mutableStateOf<PlacesService?>(null) }

    // Categorias e filtros
    var selectedCategories by remember { mutableStateOf<List<Int>>(emptyList()) }

    // Definir as categorias disponíveis
    val categories = remember {
        listOf(
            Category(1, "Jiu Jitsu", Color(0xFFFF6B6B)),
            Category(2, "T.I", Color(0xFF4ECDC4)),
            Category(3, "Centro Cultural", Color(0xFFFFD93D)),
            Category(4, "Biblioteca", Color(0xFF6C5CE7))
        )
    }

    // Limites do Brasil para o mapa
    val brasilBounds = LatLngBounds(
        LatLng(-33.7505, -73.9872), // Sudoeste
        LatLng(5.2719, -34.7299)     // Nordeste
    )

    // Inicializar LocationManager, PlacesService e verificar permissão ao entrar na tela
    LaunchedEffect(Unit) {
        locationManager = LocationManager(context)
        placesService = PlacesService(context)

        // Verificar se tem permissão de localização
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            // Se tem permissão, buscar localização
            locationManager?.getCurrentLocation { location ->
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)

                    // Buscar instituições próximas quando obtiver localização
                    scope.launch {
                        carregarInstituicoes(
                            userLocation!!,
                            placesService,
                            onInstituicoesCadastradas = { instituicoesCadastradas = it },
                            onInstituicoesNaoCadastradas = { instituicoesNaoCadastradas = it },
                            onLoading = { isLoadingInstituicoes = it }
                        )
                    }
                }
            }
        } else {
            // Se não tem permissão, mostrar diálogo
            showLocationDialog = true
        }
    }

    // Função de busca
    fun buscarInstituicoes(termo: String) {
        if (termo.isBlank()) return
        isLoading = true

        RetrofitFactory().getInstituicaoService().buscarComFiltro(termo, 1, 20)
            .enqueue(object : Callback<InstituicaoListResponse> {
                override fun onResponse(
                    call: Call<InstituicaoListResponse>,
                    response: Response<InstituicaoListResponse>
                ) {
                    isLoading = false
                    if (response.isSuccessful) {
                        val result = response.body()
                        searchResults = if (result?.status == true) {
                            result.instituicoes
                        } else emptyList()
                    } else {
                        searchResults = emptyList()
                    }
                }

                override fun onFailure(call: Call<InstituicaoListResponse>, t: Throwable) {
                    isLoading = false
                    t.printStackTrace()
                    searchResults = emptyList()
                }
            })
    }

    // Função para filtrar ONGs por categorias
    fun filtrarOngsPorCategoria(categoriaId: Int) {
        if (selectedCategories.contains(categoriaId)) {
            selectedCategories = selectedCategories.filter { it != categoriaId }
        } else {
            selectedCategories = selectedCategories + categoriaId
        }

        // Aqui você pode chamar uma API para buscar ONGs com as categorias selecionadas
        if (selectedCategories.isNotEmpty()) {
            // Exemplo: buscarOngsPorCategorias(selectedCategories)
            Log.d("HomeScreen", "Categorias selecionadas: $selectedCategories")
        }
    }

    // limpa resultados quando query ficar vazia
    LaunchedEffect(query) {
        if (query.isBlank()) {
            searchResults = emptyList()
        }
    }

    // === Mapa ===
    val initialLatLng = userLocation ?: LatLng(-15.7801, -47.9292) // Centro do Brasil
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 12f)
    }

    // Atualizar posição da câmera quando a localização do usuário é obtida
    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation!!, 13f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ===== Mapa de fundo =====
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false,
                latLngBoundsForCameraTarget = brasilBounds, // Limitar ao Brasil
                minZoomPreference = 4f,
                maxZoomPreference = 20f
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            // Marcador de localização do usuário (Azul)
            if (userLocation != null) {
                Marker(
                    state = rememberMarkerState(position = userLocation!!),
                    title = "Você está aqui",
                    snippet = "Sua localização atual",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // Marcadores de instituições CADASTRADAS (Verde)
            instituicoesCadastradas.forEach { instituicao ->
                val lat = instituicao.endereco?.latitude
                val lng = instituicao.endereco?.longitude

                if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                    Marker(
                        state = rememberMarkerState(position = LatLng(lat, lng)),
                        title = instituicao.nome,
                        snippet = "Instituição cadastrada\n${instituicao.endereco?.logradouro ?: ""}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                        onClick = {
                            // Aqui você pode adicionar ação ao clicar no marcador
                            Log.d("HomeScreen", "Clicou na instituição: ${instituicao.nome}")
                            true
                        }
                    )
                }
            }

            // Marcadores de instituições NÃO CADASTRADAS - Google Places (Laranja)
            instituicoesNaoCadastradas.forEach { place ->
                Marker(
                    state = rememberMarkerState(position = LatLng(place.latitude, place.longitude)),
                    title = place.nome,
                    snippet = "Instituição não cadastrada\n${place.endereco ?: ""}",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                    onClick = {
                        Log.d("HomeScreen", "Clicou em instituição não cadastrada: ${place.nome}")
                        true
                    }
                )
            }
        }

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

        // ===== Filtro de categorias =====
        CategoryFilterRow(
            categories = categories,
            selectedCategories = selectedCategories,
            onCategorySelected = { categoriaId ->
                filtrarOngsPorCategoria(categoriaId)
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
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
                                text = ong.nome,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
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

        // ===== Botão de atualizar localização =====
        FloatingActionButton(
            onClick = {
                // Buscar localização novamente
                locationManager?.getCurrentLocation { location ->
                    if (location != null) {
                        userLocation = LatLng(location.latitude, location.longitude)

                        // Recarregar instituições próximas
                        scope.launch {
                            carregarInstituicoes(
                                userLocation!!,
                                placesService,
                                onInstituicoesCadastradas = { instituicoesCadastradas = it },
                                onInstituicoesNaoCadastradas = { instituicoesNaoCadastradas = it },
                                onLoading = { isLoadingInstituicoes = it }
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 90.dp, start = 16.dp),
            containerColor = Color(0xFFF69508)
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Minha Localização", tint = Color.White)
        }

        // ===== Botão flutuante =====
        FloatingActionButton(
            onClick = {
                navController?.navigate("child_register")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp),
            containerColor = Color(0xFF424242)
        ) {
            Icon(Icons.Filled.Face, contentDescription = "Usuários", tint = Color.White)
        }

        // ===== Barra inferior =====
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BarraTarefas(navController = navController)
        }
    }

    // ===== Diálogo de permissão de localização =====
    if (showLocationDialog) {
        LocationPermissionDialog(
            onDismiss = {
                showLocationDialog = false
            },
            onConfirm = {
                showLocationDialog = false
            },
            context = context,
            onLocationPermissionGranted = {
                // Aguardar um pouco e tentar obter localização novamente
                locationManager?.getCurrentLocation { location ->
                    if (location != null) {
                        userLocation = LatLng(location.latitude, location.longitude)

                        // Buscar instituições próximas
                        scope.launch {
                            carregarInstituicoes(
                                userLocation!!,
                                placesService,
                                onInstituicoesCadastradas = { instituicoesCadastradas = it },
                                onInstituicoesNaoCadastradas = { instituicoesNaoCadastradas = it },
                                onLoading = { isLoadingInstituicoes = it }
                            )
                        }
                    }
                }
            }
        )
    }
}

/**
 * Função para carregar instituições cadastradas e não cadastradas
 */
private suspend fun carregarInstituicoes(
    localizacao: LatLng,
    placesService: PlacesService?,
    onInstituicoesCadastradas: (List<Instituicao>) -> Unit,
    onInstituicoesNaoCadastradas: (List<PlaceInstituicao>) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    onLoading(true)

    try {
        // 1. Buscar instituições CADASTRADAS da API
        Log.d("HomeScreen", "🔄 Buscando instituições cadastradas...")
        val response = RetrofitFactory().getInstituicaoService().listarTodasSuspend()

        if (response.isSuccessful) {
            val instituicoes = response.body()?.instituicoes ?: emptyList()

            // Filtrar apenas instituições com coordenadas válidas
            val instituicoesComLocalizacao = instituicoes.filter { inst ->
                inst.endereco?.latitude != null &&
                inst.endereco.longitude != null &&
                inst.endereco.latitude != 0.0 &&
                inst.endereco.longitude != 0.0
            }

            onInstituicoesCadastradas(instituicoesComLocalizacao)
            Log.d("HomeScreen", "✅ ${instituicoesComLocalizacao.size} instituições cadastradas carregadas")
        } else {
            Log.e("HomeScreen", "❌ Erro ao buscar instituições cadastradas: ${response.code()}")
            onInstituicoesCadastradas(emptyList())
        }

        // 2. Buscar instituições NÃO CADASTRADAS do Google Places
        if (placesService != null) {
            Log.d("HomeScreen", "🔄 Buscando instituições não cadastradas (Google Places)...")
            val instituicoesPlaces = placesService.buscarInstituicoesProximas(localizacao, raioKm = 10.0)
            onInstituicoesNaoCadastradas(instituicoesPlaces)
            Log.d("HomeScreen", "✅ ${instituicoesPlaces.size} instituições não cadastradas encontradas")
        } else {
            Log.w("HomeScreen", "⚠️ PlacesService não inicializado")
            onInstituicoesNaoCadastradas(emptyList())
        }

    } catch (e: Exception) {
        Log.e("HomeScreen", "❌ Erro ao carregar instituições", e)
        onInstituicoesCadastradas(emptyList())
        onInstituicoesNaoCadastradas(emptyList())
    } finally {
        onLoading(false)
    }
}

/**
 * Diálogo para solicitar permissão de localização
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    context: android.content.Context,
    onLocationPermissionGranted: () -> Unit
) {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permissão de Localização") },
        text = { Text("Este aplicativo precisa acessar sua localização para mostrar instituições próximas a você.") },
        confirmButton = {
            Button(
                onClick = {
                    permissionState.launchPermissionRequest()
                    onConfirm()
                }
            ) {
                Text("Permitir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Observar mudanças na permissão
    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            onLocationPermissionGranted()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = null)
}
