package com.oportunyfam_mobile.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.oportunyfam_mobile.Components.CategoryFilterRow
import com.oportunyfam_mobile.Components.Category
import com.oportunyfam_mobile.Components.OngMapMarkers
import com.oportunyfam_mobile.Components.getExampleOngs
import com.oportunyfam_mobile.Components.filterOngsByCategories

/**
 * Exemplo de uso completo do sistema de filtro de categorias
 */
@Composable
fun ExampleHomeScreenWithFilters() {
    // Estados
    var selectedCategories by remember { mutableStateOf<List<Int>>(emptyList()) }
    var showingOngCount by remember { mutableStateOf(0) }

    // Categorias
    val categories = remember {
        listOf(
            Category(1, "Jiu Jitsu", Color(0xFFFF6B6B)),
            Category(2, "T.I", Color(0xFF4ECDC4)),
            Category(3, "Centro Cultural", Color(0xFFFFD93D)),
            Category(4, "Biblioteca", Color(0xFF6C5CE7))
        )
    }

    // Obter todas as ONGs
    val allOngs = remember { getExampleOngs() }

    // Filtrar ONGs de acordo com as categorias selecionadas
    val filteredOngs = remember(selectedCategories) {
        if (selectedCategories.isEmpty()) {
            emptyList()
        } else {
            filterOngsByCategories(allOngs, selectedCategories)
        }
    }

    // Atualizar contador
    LaunchedEffect(filteredOngs) {
        showingOngCount = filteredOngs.size
    }

    // Mapa
    val initialLatLng = LatLng(-23.5505, -46.6333)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 12f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa de fundo
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            // Adicionar marcadores das ONGs filtradas
            OngMapMarkers(
                ongs = filteredOngs,
                onMarkerClick = { ong ->
                    println("Clicou em: ${ong.nome}")
                }
            )
        }

        // Filtro de categorias na parte superior
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Filtro de categorias
            CategoryFilterRow(
                categories = categories,
                selectedCategories = selectedCategories,
                onCategorySelected = { categoriaId ->
                    if (selectedCategories.contains(categoriaId)) {
                        selectedCategories = selectedCategories.filter { it != categoriaId }
                    } else {
                        selectedCategories = selectedCategories + categoriaId
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Mostrar quantidade de ONGs encontradas
            if (selectedCategories.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    )
                ) {
                    Text(
                        text = "📍 $showingOngCount ONG(s) encontrada(s)",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

/**
 * Preview do exemplo
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PreviewExampleHomeScreenWithFilters() {
    ExampleHomeScreenWithFilters()
}

