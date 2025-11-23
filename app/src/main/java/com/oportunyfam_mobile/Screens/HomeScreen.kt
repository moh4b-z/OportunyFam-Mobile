package com.oportunyfam_mobile.Screens

import android.Manifest
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.SearchBar
import com.oportunyfam_mobile.Components.CategoryFilterRow
import com.oportunyfam_mobile.Components.Category
import com.oportunyfam_mobile.Components.OngMapMarkers
import com.oportunyfam_mobile.model.OngMapMarker
import com.oportunyfam_mobile.Service.LocationManager
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.model.InstituicaoListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues

@Composable
fun HomeScreen(navController: NavHostController?) {
    // === Contexto ===
    val context = LocalContext.current

    // Check location permission at composition time to drive MapProperties
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // === Estados ===
    var query by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Instituicao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    // Estado para carregamento de ONGs por categoria
    var isLoadingCategories by remember { mutableStateOf(false) }
    // ONGs filtradas (marcadores)
    var filteredOngs by remember { mutableStateOf<List<OngMapMarker>>(emptyList()) }

    // Estados de localização
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationManager by remember { mutableStateOf<LocationManager?>(null) }

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

    // Inicializar LocationManager e verificar permissão ao entrar na tela
    LaunchedEffect(Unit) {
        locationManager = LocationManager(context)

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

        // Se há categorias selecionadas, buscar na API
        if (selectedCategories.isNotEmpty()) {
            isLoadingCategories = true
            val csv = selectedCategories.joinToString(",")
            RetrofitFactory().getInstituicaoService()
                .buscarPorCategorias(csv, 1, 100)
                .enqueue(object : Callback<InstituicaoListResponse> {
                    override fun onResponse(
                        call: Call<InstituicaoListResponse>,
                        response: Response<InstituicaoListResponse>
                    ) {
                        isLoadingCategories = false
                        if (response.isSuccessful) {
                            val body = response.body()
                            val institutos = if (body?.status == true) body.instituicoes else emptyList()

                            // Mapear Instituicao -> OngMapMarker (filtrar sem coordenadas)
                            filteredOngs = institutos.mapNotNull { inst ->
                                val lat = inst.endereco?.latitude
                                val lon = inst.endereco?.longitude
                                if (lat == null || lon == null) return@mapNotNull null

                                OngMapMarker(
                                    id = inst.instituicao_id,
                                    nome = inst.nome,
                                    latitude = lat,
                                    longitude = lon,
                                    categorias = emptyList(),
                                    descricao = inst.descricao ?: "",
                                    endereco = inst.endereco?.logradouro ?: "",
                                    telefone = inst.telefone ?: "",
                                    email = inst.email ?: ""
                                )
                            }
                        } else {
                            filteredOngs = emptyList()
                        }
                    }

                    override fun onFailure(call: Call<InstituicaoListResponse>, t: Throwable) {
                        isLoadingCategories = false
                        t.printStackTrace()
                        filteredOngs = emptyList()
                    }
                })
        } else {
            // Sem categorias selecionadas limpa os marcadores
            filteredOngs = emptyList()
        }
    }

    // limpa resultados quando query ficar vazia
    LaunchedEffect(query) {
        if (query.isBlank()) {
            searchResults = emptyList()
        }
    }

    // === Mapa ===
    val initialLatLng = userLocation ?: LatLng(-23.5505, -46.6333)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
    }

    // Atualizar posição da câmera quando a localização do usuário é obtida
    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation!!, 15f)
        }
    }

    // altura interna usada pela barra de tarefas definida no componente (ver BarraTarefas: .height(64.dp))
    val barraHeight = 64.dp
    // inset do navigation bar do sistema (p.ex. botões home/back) — será 0 em gesture navigation
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomReserved = barraHeight + navBarInset

    Box(modifier = Modifier.fillMaxSize()) {

        // ===== Mapa de fundo =====
        // garante que o conteúdo do mapa não fique por baixo da área da barra de tarefas + nav bar
        GoogleMap(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = bottomReserved),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapType = MapType.NORMAL // Force default Google Maps rendering
            ),
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            onMapLoaded = {
                android.util.Log.d("HomeScreen", "GoogleMap onMapLoaded - hasLocationPermission=$hasLocationPermission")
            }
        ) {
            // Adicionar marcador de localização do usuário
            if (userLocation != null) {
                Marker(
                    state = rememberMarkerState(position = userLocation!!),
                    title = "Sua Localização",
                    snippet = "Você está aqui"
                )
            }


            // Adicionar marcadores das ONGs filtradas (se houver)
            if (filteredOngs.isNotEmpty()) {
                OngMapMarkers(
                    ongs = filteredOngs,
                    onMarkerClick = { ong ->
                        // Navegar para o perfil da instituição
                        navController?.navigate("instituicao_perfil/${ong.id}")
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
                .statusBarsPadding()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
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
                .padding(top = 84.dp)
        )

        // ===== Resultados =====
        if (searchResults.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(top = 100.dp)
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
                                    // Navegar para o perfil da instituição quando clicar no resultado
                                    navController?.navigate("instituicao_perfil/${ong.instituicao_id}")
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
                        .padding(top = 100.dp)
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
                locationManager?.getCurrentLocation { location ->
                    if (location != null) {
                        userLocation = LatLng(location.latitude, location.longitude)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp + bottomReserved),
            containerColor = Color(0xFFF69508)
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Minha Localização", tint = Color.White)
        }

        // ===== Botão flutuante =====
        FloatingActionButton(
            onClick = { navController?.navigate("child_register") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp + bottomReserved),
            containerColor = Color(0xFF424242)
        ) {
            Icon(Icons.Filled.Face, contentDescription = "Usuários", tint = Color.White)
        }

        // ===== Barra inferior (overlay) =====
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            // `BarraTarefas` já aplica navigationBarsPadding() internamente e define altura 64.dp
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
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = null)
}
