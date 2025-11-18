package com.oportunyfam_mobile.Screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
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
import com.oportunyfam_mobile.Components.MapComponent
import com.oportunyfam_mobile.Components.SearchBar
import com.oportunyfam_mobile.Components.CategoryFilterRow
import com.oportunyfam_mobile.Components.Category
import com.oportunyfam_mobile.Service.LocationManager
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.Service.PlacesService
import com.oportunyfam_mobile.Service.PlaceInstituicao
import com.oportunyfam_mobile.model.Instituicao
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import com.google.android.gms.maps.MapsInitializer
import com.oportunyfam_mobile.util.haversineKm
import com.oportunyfam_mobile.util.normalizeCep
import java.util.Locale

private const val TAG = "HomeScreen"

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
    // Resultados quando o usuário filtra por categorias
    var categoryResults by remember { mutableStateOf<List<Instituicao>>(emptyList()) }

    // Estados de localização
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationManager by remember { mutableStateOf<LocationManager?>(null) }
    var placesService by remember { mutableStateOf<PlacesService?>(null) }
    var isMapReady by remember { mutableStateOf(false) }

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

    /**
     * Função para carregar instituições cadastradas e não cadastradas
     */
    suspend fun carregarInstituicoes(
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

    // Inicializar LocationManager, PlacesService e verificar permissão ao entrar na tela
    LaunchedEffect(Unit) {
        try {
            // Inicializar Google Maps
            MapsInitializer.initialize(context)
            isMapReady = true
            Log.d("HomeScreen", "✅ Google Maps inicializado com sucesso")
        } catch (e: Exception) {
            Log.e("HomeScreen", "❌ Erro ao inicializar Google Maps: ${e.message}")
        }

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
                    Log.d("HomeScreen", "📍 Localização obtida: ${location.latitude}, ${location.longitude}")

                    // Buscar instituições próximas quando obtiver localização
                    scope.launch {
                        carregarInstituicoes(
                            userLocation!!,
                            placesService,
                            onInstituicoesCadastradas = { instituicoesCadastradas = it },
                            onInstituicoesNaoCadastradas = { instituicoesNaoCadastradas = it },
                            onLoading = {}
                        )
                    }
                } else {
                    Log.w("HomeScreen", "⚠️ Localização não obtida")
                }
            }
        } else {
            // Se não tem permissão, mostrar diálogo
            Log.w("HomeScreen", "⚠️ Sem permissão de localização")
            showLocationDialog = true
        }
    }

    // Função de busca: tenta geocoding para obter CEP/lat-lng e filtrar localmente por CEP ou distância
    fun buscarInstituicoes(termo: String) {
        if (termo.isBlank()) return
        isLoading = true

        scope.launch {
            try {
                // Carregar todas as instituições uma vez (poderia ser cacheado)
                val response = RetrofitFactory().getInstituicaoService().listarTodasSuspend()
                val todasInst = if (response.isSuccessful) response.body()?.instituicoes ?: emptyList() else emptyList()

                // Usar Geocoder para tentar obter postalCode / lat-lng do termo pesquisado
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = try {
                    geocoder.getFromLocationName(termo, 5) ?: emptyList()
                } catch (e: Exception) {
                    Log.w("HomeScreen", "Geocoder falhou: ${e.message}")
                    emptyList()
                }

                val resultados: List<Instituicao> = if (addresses.isNotEmpty()) {
                    val address = addresses.first()
                    val postal = normalizeCep(address.postalCode)
                    val lat = address.latitude
                    val lng = address.longitude

                    if (postal != null) {
                        // Filtrar por CEP
                        todasInst.filter { inst ->
                            val instCep = normalizeCep(inst.endereco?.cep)
                            instCep != null && instCep == postal
                        }
                    } else if (lat != 0.0 && lng != 0.0) {
                        // Filtrar por distância (raio configurável)
                        val raioKm = 5.0
                        todasInst.mapNotNull { inst ->
                            val ilat = inst.endereco?.latitude
                            val ilng = inst.endereco?.longitude
                            if (ilat == null || ilng == null) return@mapNotNull null
                            val d = haversineKm(lat, lng, ilat, ilng)
                            if (d <= raioKm) inst else null
                        }
                    } else emptyList()
                } else {
                    // Se não obteve endereço, fallback para endpoint de busca por texto
                    val call = RetrofitFactory().getInstituicaoService().buscarComFiltro(termo, 1, 20)
                    val resp = call.execute()
                    if (resp.isSuccessful) resp.body()?.instituicoes ?: emptyList() else emptyList()
                }

                searchResults = resultados
            } catch (e: Exception) {
                Log.e("HomeScreen", "Erro ao buscar instituições: ${e.message}")
                searchResults = emptyList()
            } finally {
                isLoading = false
            }
        }
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

    // Recalcula os resultados por categoria sempre que a lista de instituições
    // cadastradas ou as categorias selecionadas mudarem.
    LaunchedEffect(instituicoesCadastradas, selectedCategories) {
        if (selectedCategories.isEmpty()) {
            categoryResults = emptyList()
        } else {
            categoryResults = instituicoesCadastradas.filter { inst ->
                inst.tipos_instituicao.any { tipo ->
                    val tipoId = tipo.id ?: -1
                    selectedCategories.contains(tipoId)
                }
            }
            Log.d("HomeScreen", "🔎 ${categoryResults.size} instituições encontradas para categorias $selectedCategories")
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
        if (isMapReady) {
            MapComponent(
                userLocation = userLocation,
                instituicoesCadastradas = instituicoesCadastradas,
                instituicoesNaoCadastradas = instituicoesNaoCadastradas,
                selectedCategories = selectedCategories,
                categoryResults = categoryResults,
                cameraPositionState = cameraPositionState,
                brasilBounds = brasilBounds,
                isMapReady = isMapReady,
                onMapLoaded = {
                    Log.i(TAG, "✅ Callback onMapLoaded chamado")
                },
                onMapClick = { latLng ->
                    Log.d(TAG, "🖱️ Clique no mapa na HomeScreen")
                },
                navController = navController
            )
        } else {
            // Mostrar loading enquanto o mapa está sendo inicializado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Carregando mapa...",
                        modifier = Modifier.padding(top = 16.dp),
                        color = Color.Gray
                    )
                }
            }
        }

        // ===== Barra de pesquisa =====
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                // Navegar para tela de busca
                if (it.isNotBlank()) {
                    navController?.navigate("search_results/$it")
                }
            },
            onSearchIconClick = {
                // Navegar para tela de busca
                if (query.isNotBlank()) {
                    navController?.navigate("search_results/$query")
                }
            },
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
        // Prioridade: quando há filtro por categoria, mostramos `categoryResults`.
        // Combine filtros: se houver resultado de busca (texto/CEP) e categorias selecionadas,
        // aplicamos INTERSECÇÃO (ambos must match). Caso contrário, se houver apenas categorias,
        // mostramos `categoryResults`. Senão mostramos `searchResults`.
        val resultsToShowBase = when {
            searchResults.isNotEmpty() && selectedCategories.isNotEmpty() -> searchResults.filter { inst ->
                inst.tipos_instituicao.any { tipo -> selectedCategories.contains(tipo.id ?: -1) }
            }
            selectedCategories.isNotEmpty() -> categoryResults
            else -> searchResults
        }

        // Anexar distância quando possível e ordenar por proximidade se temos `userLocation`.
        val resultsWithDistance: List<Pair<Instituicao, Double?>> = resultsToShowBase.map { inst ->
            val ilat = inst.endereco?.latitude
            val ilng = inst.endereco?.longitude
            val dist = if (userLocation != null && ilat != null && ilng != null && ilat != 0.0 && ilng != 0.0) {
                haversineKm(userLocation!!.latitude, userLocation!!.longitude, ilat, ilng)
            } else null
            Pair(inst, dist)
        }

        val resultsToShow = if (userLocation != null) {
            // ordenar por distância (nulls no final)
            resultsWithDistance.sortedWith(compareBy<Pair<Instituicao, Double?>> { it.second ?: Double.MAX_VALUE }).map { it.first }
        } else resultsToShowBase

        if (resultsToShow.isNotEmpty()) {
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
                    // Recriar lista com distâncias (se disponível) para exibir no item
                    val rowsToShow = if (userLocation != null) {
                        // já ordenado por distância; recriar pares dist novamente para exibir
                        resultsToShow.map { inst ->
                            val ilat = inst.endereco?.latitude
                            val ilng = inst.endereco?.longitude
                            val dist = if (userLocation != null && ilat != null && ilng != null && ilat != 0.0 && ilng != 0.0) {
                                haversineKm(userLocation!!.latitude, userLocation!!.longitude, ilat, ilng)
                            } else null
                            Pair(inst, dist)
                        }
                    } else {
                        resultsToShow.map { Pair(it, null) }
                    }

                    rowsToShow.forEach { (ong, dist) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Navegar para perfil da instituição
                                    Log.d("HomeScreen", "Clicou em resultado: ${ong.nome} (ID: ${ong.instituicao_id})")
                                    navController?.navigate("instituicao_perfil/${ong.instituicao_id}")
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ong.nome,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            if (dist != null) {
                                Text(
                                    text = String.format(Locale.US, "%.1f km", dist),
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                    }
                }
            }
        }

        // ===== Indicador de carregamento =====
        val noResults = (selectedCategories.isNotEmpty() && (categoryResults.isEmpty() || resultsToShowBase.isEmpty())) || (selectedCategories.isEmpty() && resultsToShow.isEmpty() && query.isNotBlank())

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 90.dp)
                )
            }

            noResults -> {
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
                                onLoading = {}
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

    // Observação: o diálogo de permissão foi movido para `LocationPermissionDialog.kt`.
    // O controle `showLocationDialog` continua disponível e a chamada usa a função centralizada.

    // Mostrar diálogo de permissão quando necessário (usa o diálogo central em LocationPermissionDialog.kt)
    if (showLocationDialog) {
        LocationPermissionDialog(
            onDismiss = { showLocationDialog = false },
            onConfirm = { showLocationDialog = false },
            context = context,
            onLocationPermissionGranted = {
                // Após usuário ativar/permissão concedida, tentar obter localização e recarregar instituições
                locationManager?.getCurrentLocation { location ->
                    if (location != null) {
                        userLocation = LatLng(location.latitude, location.longitude)
                        scope.launch {
                            carregarInstituicoes(
                                userLocation!!,
                                placesService,
                                onInstituicoesCadastradas = { instituicoesCadastradas = it },
                                onInstituicoesNaoCadastradas = { instituicoesNaoCadastradas = it },
                                onLoading = {}
                            )
                        }
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
