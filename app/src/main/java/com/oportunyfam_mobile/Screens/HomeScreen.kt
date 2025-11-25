@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.oportunyfam_mobile.Screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.data.AuthType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.oportunyfam_mobile.Service.fetchPlaceDetails

@Composable
fun HomeScreen(navController: NavHostController?, showCreateChild: Boolean = false) {
    // === Contexto ===
    val context = LocalContext.current
    val authDataStore = remember { AuthDataStore(context) }
    var authUser by remember { mutableStateOf<com.oportunyfam_mobile.data.AuthUserWrapper?>(null) }
    var shouldShowCreateChildCard by remember { mutableStateOf(showCreateChild) }

    // SharedPreferences para persistir o 'pular' do card
    val prefs = remember { context.getSharedPreferences("oportunyfam_prefs", Context.MODE_PRIVATE) }
    var createChildPromptDismissed by remember { mutableStateOf(prefs.getBoolean("create_child_prompt_dismissed", false)) }

    // Carrega o estado de autenticação
    LaunchedEffect(Unit) {
        val loaded = authDataStore.loadAuthUser()
        authUser = loaded
        // garante que o estado do dismiss seja atualizado caso mude externamente
        createChildPromptDismissed = prefs.getBoolean("create_child_prompt_dismissed", false)
    }

    // Check location permission at composition time to drive MapProperties
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // === Estados ===
    var query by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Instituicao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Estados de localização
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationManager by remember { mutableStateOf<LocationManager?>(null) }

    // Marcadores de API + externos (estado)
    var apiMarkers by remember { mutableStateOf<List<OngMapMarker>>(emptyList()) }
    var externalMarkers by remember { mutableStateOf<List<OngMapMarker>>(emptyList()) }

    // estado para marker externo selecionado e detalhes
    var selectedExternalMarker by remember { mutableStateOf<OngMapMarker?>(null) }
    var selectedExternalDetails by remember { mutableStateOf<com.oportunyfam_mobile.Service.PlaceDetailsResult?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Estados para controlar carregamento dinâmico ao mover o mapa
    var isLoadingExternalMarkers by remember { mutableStateOf(false) }
    var lastLoadedPosition by remember { mutableStateOf<LatLng?>(null) }

    // Estado para filtro de categoria selecionada
    var selectedCategoryType by remember { mutableStateOf<String?>(null) }

    // Definir as categorias disponíveis (baseadas nos tipos do Google Places)
    data class PlaceCategory(val id: String, val displayName: String, val color: Color)

    val categories = remember {
        listOf(
            PlaceCategory("school", "Escolas", Color(0xFF4ECDC4)),
            PlaceCategory("library", "Bibliotecas", Color(0xFF6C5CE7)),
            PlaceCategory("gym", "Academias", Color(0xFFFF6B6B)),
            PlaceCategory("point_of_interest", "Pontos de Interesse", Color(0xFFFFD93D))
        )
    }

    // initial camera position (moved up so LaunchedEffect can use it)
    val initialLatLng = userLocation ?: LatLng(-23.5505, -46.6333)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
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

    // Função para carregar lugares externos em uma posição específica
    suspend fun loadExternalMarkers(lat: Double, lon: Double, typeFilter: String? = null) {
        if (isLoadingExternalMarkers) return

        // Se houver filtro, sempre recarregar (não verificar distância)
        // Se não houver filtro, verificar se já carregamos recentemente nesta posição
        if (typeFilter == null) {
            val currentPos = LatLng(lat, lon)
            lastLoadedPosition?.let { lastPos ->
                val distance = FloatArray(1)
                android.location.Location.distanceBetween(
                    lastPos.latitude, lastPos.longitude,
                    currentPos.latitude, currentPos.longitude,
                    distance
                )
                // Se a distância for menor que 500 metros, não recarregar
                if (distance[0] < 500) return
            }
            lastLoadedPosition = currentPos
        }

        isLoadingExternalMarkers = true
        try {
            val newMarkers = com.oportunyfam_mobile.Service.fetchPlacesFromGoogle(context, lat, lon, typeFilter)

            if (typeFilter != null) {
                // Se houver filtro, substituir marcadores ao invés de mesclar
                externalMarkers = newMarkers
                android.util.Log.d("HomeScreen", "Carregados ${newMarkers.size} marcadores do tipo '$typeFilter' na posição ($lat, $lon)")
            } else {
                // Mesclar com marcadores existentes, evitando duplicatas
                val existingIds = externalMarkers.map { it.placeId }.toSet()
                val uniqueNewMarkers = newMarkers.filter { it.placeId !in existingIds }
                externalMarkers = externalMarkers + uniqueNewMarkers
                android.util.Log.d("HomeScreen", "Carregados ${uniqueNewMarkers.size} novos marcadores externos na posição ($lat, $lon)")
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeScreen", "Erro ao carregar marcadores externos: ${e.message}", e)
        } finally {
            isLoadingExternalMarkers = false
        }
    }

    // Carrega instituições da API e locais externos quando a tela inicia
    LaunchedEffect(Unit) {
        // carregar instituições da nossa API
        try {
            val response = withContext(Dispatchers.IO) { RetrofitFactory().getInstituicaoService().listarTodasSuspend() }
            if (response.isSuccessful) {
                val list = response.body()?.instituicoes ?: emptyList()
                apiMarkers = list.map { inst ->
                    OngMapMarker(
                        id = inst.instituicao_id,
                        nome = inst.nome,
                        latitude = inst.endereco?.latitude ?: 0.0,
                        longitude = inst.endereco?.longitude ?: 0.0,
                        categorias = emptyList(),
                        descricao = inst.descricao ?: "",
                        endereco = inst.endereco?.logradouro ?: "",
                        telefone = inst.telefone ?: "",
                        email = inst.email ?: "",
                        isExternal = false
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            apiMarkers = emptyList()
        }

        // carregar places externos na posição inicial
        try {
            val lat = userLocation?.latitude ?: initialLatLng.latitude
            val lon = userLocation?.longitude ?: initialLatLng.longitude
            loadExternalMarkers(lat, lon)
        } catch (e: Exception) {
            e.printStackTrace()
            externalMarkers = emptyList()
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

    // limpa resultados quando query ficar vazia
    LaunchedEffect(query) {
        if (query.isBlank()) {
            searchResults = emptyList()
        }
    }

    // Atualizar posição da câmera quando a localização do usuário é obtida
    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLocation!!, 15f)
        }
    }

    // Recarregar marcadores quando a categoria selecionada mudar
    LaunchedEffect(selectedCategoryType) {
        val center = cameraPositionState.position.target
        loadExternalMarkers(center.latitude, center.longitude, selectedCategoryType)
    }

    // marcadores que devem ser exibidos no mapa: se houver resultados da busca, mostramos apenas eles + externos;
    // caso contrário mostramos todas as instituições da API + externos
    val displayedMarkers by remember(searchResults, apiMarkers, externalMarkers) {
        val fromSearch = if (searchResults.isNotEmpty()) {
            searchResults.map { ong ->
                OngMapMarker(
                    id = ong.instituicao_id,
                    nome = ong.nome,
                    latitude = ong.endereco?.latitude ?: 0.0,
                    longitude = ong.endereco?.longitude ?: 0.0,
                    categorias = emptyList(),
                    descricao = ong.descricao ?: "",
                    endereco = ong.endereco?.logradouro ?: "",
                    telefone = ong.telefone ?: "",
                    email = ong.email ?: "",
                    isExternal = false
                )
            }
        } else apiMarkers
        mutableStateOf(fromSearch + externalMarkers)
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
            },
            onMapClick = { /* Opcional: esconder resultados de busca ao clicar no mapa */ }
        ) {
            // Listener para carregar novos lugares quando o mapa parar de mover
            LaunchedEffect(cameraPositionState.isMoving) {
                if (!cameraPositionState.isMoving) {
                    // Mapa parou de mover, carregar lugares na nova posição
                    val center = cameraPositionState.position.target
                    android.util.Log.d("HomeScreen", "Mapa parou de mover em (${center.latitude}, ${center.longitude})")

                    // Carregar novos marcadores externos com o filtro atual
                    coroutineScope.launch {
                        loadExternalMarkers(center.latitude, center.longitude, selectedCategoryType)
                    }
                }
            }
            // marcador do usuário
            if (userLocation != null) {
                Marker(state = rememberMarkerState(position = userLocation!!), title = "Sua Localização")
            }

            // Render displayed markers (API + external)
            if (displayedMarkers.isNotEmpty()) {
                OngMapMarkers(
                    ongs = displayedMarkers,
                    onMarkerClick = { marker ->
                        if (!marker.isExternal) {
                            navController?.navigate("instituicao_perfil/${marker.id}")
                        } else {
                            // abrir bottom sheet com detalhes
                            selectedExternalMarker = marker
                            selectedExternalDetails = null
                            coroutineScope.launch {
                                sheetState.show()
                            }

                            // buscar detalhes em background
                            coroutineScope.launch {
                                val details = fetchPlaceDetails(context, marker.placeId ?: "")
                                selectedExternalDetails = details
                            }
                        }
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

        // ===== Filtros de Categoria =====
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 68.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botão "Todos"
                FilterChip(
                    selected = selectedCategoryType == null,
                    onClick = {
                        selectedCategoryType = null
                    },
                    label = { Text("Todos") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFA000),
                        selectedLabelColor = Color.White,
                        containerColor = Color.LightGray.copy(alpha = 0.3f),
                        labelColor = Color.DarkGray
                    )
                )

                // Botões de categoria
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryType == category.id,
                        onClick = {
                            selectedCategoryType = if (selectedCategoryType == category.id) null else category.id
                        },
                        label = { Text(category.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = category.color,
                            selectedLabelColor = Color.White,
                            containerColor = Color.LightGray.copy(alpha = 0.3f),
                            labelColor = Color.DarkGray
                        )
                    )
                }
            }
        }

        // ===== Resultados =====
        if (searchResults.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(top = 128.dp)
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
                        .padding(top = 140.dp)
                )
            }

            searchResults.isEmpty() && query.isNotBlank() -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 128.dp)
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

        // ===== Indicador de carregamento de marcadores externos =====
        if (isLoadingExternalMarkers) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFFF69508),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Carregando locais...",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
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
        // Mostrar o FAB de criar criança somente para usuários (não para logins de criança)
        if (authUser?.type == AuthType.USUARIO) {
            FloatingActionButton(
                onClick = { navController?.navigate("child_register") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp + bottomReserved),
                containerColor = Color(0xFF424242)
            ) {
                Icon(Icons.Filled.Face, contentDescription = "Usuários", tint = Color.White)
            }
        }

        // ===== Barra inferior (overlay) =====
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            // `BarraTarefas` já aplica navigationBarsPadding() internamente e define altura 64.dp
            BarraTarefas(navController = navController)
        }

        // Mostrar o card de criar filho apenas se: flag passada == true, usuário é do tipo USUARIO e não foi dismiss persistido
        val isUsuario = authUser?.type == AuthType.USUARIO
        if (shouldShowCreateChildCard && isUsuario && !createChildPromptDismissed) {
            CreateChildPromptCard(
                onCreate = {
                    shouldShowCreateChildCard = false
                    navController?.navigate("child_register")
                },
                onSkip = {
                    // persiste o skip para não mostrar novamente
                    prefs.edit().putBoolean("create_child_prompt_dismissed", true).apply()
                    createChildPromptDismissed = true
                    shouldShowCreateChildCard = false
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            )
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

    // ===== Bottom Sheet com detalhes de local externo =====
    if (selectedExternalMarker != null) {
        ModalBottomSheet(onDismissRequest = {
            coroutineScope.launch { sheetState.hide() }
            selectedExternalMarker = null
        }, sheetState = sheetState) {
            val m = selectedExternalMarker
            val primaryColor = Color(0xFFF69508)
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            val toastContext = context
            // Card container to control background tone (warm white) and rounded corners
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E0)),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (m != null) {
                        Text(m.nome, style = MaterialTheme.typography.titleLarge, color = Color(0xFF222222))
                        Spacer(Modifier.height(8.dp))
                        if (!m.descricao.isNullOrBlank()) Text(m.descricao, color = Color(0xFF444444))
                        // se temos detalhes mais ricos, mostrar
                        val d = selectedExternalDetails
                        Spacer(Modifier.height(8.dp))
                        if (d != null) {
                            // Endereço — clicável para copiar
                            d.formatted_address?.takeIf { it.isNotBlank() }?.let { addr ->
                                Text(text = "Endereço:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF666666))
                                Text(
                                    text = addr,
                                    color = primaryColor,
                                    modifier = Modifier
                                        .padding(top = 4.dp, bottom = 6.dp)
                                        .clickable {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(addr))
                                            android.widget.Toast.makeText(toastContext, "Endereço copiado", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }

                            // Telefone — clicável para copiar
                            d.formatted_phone_number?.takeIf { it.isNotBlank() }?.let { phone ->
                                Text(text = "Telefone:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF666666))
                                Text(
                                    text = phone,
                                    color = primaryColor,
                                    modifier = Modifier
                                        .padding(top = 4.dp, bottom = 6.dp)
                                        .clickable {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(phone))
                                            android.widget.Toast.makeText(toastContext, "Telefone copiado", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }

                            // Website — clicável para abrir
                            d.website?.takeIf { it.isNotBlank() }?.let { site ->
                                Text(text = "Website:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF666666))
                                Text(
                                    text = site,
                                    color = primaryColor,
                                    modifier = Modifier
                                        .padding(top = 4.dp, bottom = 6.dp)
                                        .clickable {
                                            val uri = android.net.Uri.parse(site)
                                            toastContext.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                                        }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    val uri = if (!m.placeId.isNullOrBlank()) {
                                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(m.nome)}&query_place_id=${m.placeId}")
                                    } else {
                                        Uri.parse("geo:${m.latitude},${m.longitude}?q=${Uri.encode(m.nome)}")
                                    }
                                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Text("Abrir no Google Maps", color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch { sheetState.hide() }
                                    selectedExternalMarker = null
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                                border = BorderStroke(1.dp, primaryColor)
                            ) {
                                Text("Fechar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateChildPromptCard(onCreate: () -> Unit, onSkip: () -> Unit, modifier: Modifier = Modifier) {
    // Paleta clara: fundo branco, acentos em amarelo/laranja
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Deseja cadastrar uma criança agora?", fontSize = 16.sp, color = Color.Black)
            Spacer(Modifier.height(8.dp))
            Text("Você pode adicionar dependentes para gerenciar inscrições e participar de atividades.", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onCreate, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))) {
                    Text("Criar criança", color = Color.White)
                }
                OutlinedButton(onClick = onSkip, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFA000))) {
                    Text("Pular")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = null)
}
