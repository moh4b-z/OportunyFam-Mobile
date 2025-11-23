package com.oportunyfam_mobile.Components

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.Utils.haversineKm
import java.util.Locale

private const val TAG = "MapComponent"

@Composable
fun MapComponent(
    userLocation: LatLng?,
    instituicoesCadastradas: List<Instituicao>,
    selectedCategories: List<Int>,
    categoryResults: List<Instituicao>,
    cameraPositionState: CameraPositionState,
    brasilBounds: LatLngBounds,
    isMapReady: Boolean,
    onMapLoaded: () -> Unit,
    onMapClick: (LatLng) -> Unit,
    navController: NavHostController?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Log.d(TAG, "========================================")
    Log.d(TAG, "🗺️ RENDERIZANDO MapComponent")
    Log.d(TAG, "========================================")
    Log.d(TAG, "📍 userLocation: $userLocation")
    Log.d(TAG, "📊 instituicoesCadastradas.size: ${instituicoesCadastradas.size}")
    Log.d(TAG, "🏷️ selectedCategories: $selectedCategories")
    Log.d(TAG, "📊 categoryResults.size: ${categoryResults.size}")
    Log.d(TAG, "✅ isMapReady: $isMapReady")
    Log.d(TAG, "📍 Posição da câmera: ${cameraPositionState.position.target}")
    Log.d(TAG, "🔍 Zoom: ${cameraPositionState.position.zoom}")

    // Verificar API Key
    try {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        val mapId = appInfo.metaData?.getString("com.google.android.geo.MAP_ID")

        Log.d(TAG, "🔑 API Key: ${apiKey?.take(15)}...${apiKey?.takeLast(10)}")
        Log.d(TAG, "🆔 MAP_ID: $mapId")

        if (apiKey.isNullOrEmpty()) {
            Log.e(TAG, "❌ ERRO: API Key não encontrada!")
        }
    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro ao verificar API Key: ${e.message}", e)
    }

    GoogleMap(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            latLngBoundsForCameraTarget = brasilBounds,
            minZoomPreference = 4f,
            maxZoomPreference = 20f
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        ),
        onMapLoaded = {
            Log.d(TAG, "✅ ========================================")
            Log.d(TAG, "✅ MAPA CARREGADO COM SUCESSO!")
            Log.d(TAG, "✅ ========================================")
            Log.d(TAG, "✅ Se você está vendo este log, o mapa DEVE estar visível")
            Log.d(TAG, "✅ Se o mapa ainda está preto, o problema é a API Key")
            onMapLoaded()
        },
        onMapClick = { latLng ->
            Log.d(TAG, "🖱️ Clique no mapa: Lat ${latLng.latitude}, Lng ${latLng.longitude}")
            onMapClick(latLng)
        }
    ) {
        Log.d(TAG, "🎨 Desenhando marcadores no mapa...")

        // Marcador de localização do usuário (Azul)
        if (userLocation != null) {
            Log.d(TAG, "📍 Adicionando marcador do usuário em: $userLocation")
            Marker(
                state = rememberMarkerState(position = userLocation),
                title = "Você está aqui",
                snippet = "Sua localização atual",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        } else {
            Log.w(TAG, "⚠️ Sem localização do usuário para marcador")
        }

        // Marcadores de instituições CADASTRADAS (Verde)
        val marcadoresVisiveis = if (selectedCategories.isNotEmpty()) {
            Log.d(TAG, "🏷️ Mostrando marcadores filtrados por categoria")
            categoryResults
        } else {
            Log.d(TAG, "🏢 Mostrando todos os marcadores cadastrados")
            instituicoesCadastradas
        }

        Log.d(TAG, "🏢 Adicionando ${marcadoresVisiveis.size} marcadores de instituições")

        marcadoresVisiveis.forEach { instituicao ->
            val lat = instituicao.endereco?.latitude
            val lng = instituicao.endereco?.longitude

            if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                // Calcular distância se temos localização do usuário
                val distance = if (userLocation != null) {
                    haversineKm(userLocation.latitude, userLocation.longitude, lat, lng)
                } else null
                val distanceText = if (distance != null) {
                    String.format(Locale.US, "%.1f km", distance)
                } else ""

                Log.v(TAG, "📍 Marcador: ${instituicao.nome} em ($lat, $lng) $distanceText")

                Marker(
                    state = rememberMarkerState(position = LatLng(lat, lng)),
                    title = instituicao.nome,
                    snippet = "Instituição cadastrada\n${instituicao.endereco.logradouro}\n$distanceText",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                    onClick = {
                        Log.d(TAG, "🖱️ Clicou na instituição: ${instituicao.nome}")
                        navController?.navigate("instituicao_perfil/${instituicao.instituicao_id}")
                        true
                    }
                )
            } else {
                Log.w(TAG, "⚠️ Instituição ${instituicao.nome} sem coordenadas válidas")
            }
        }

        Log.d(TAG, "✅ Marcadores desenhados com sucesso")
    }

    Log.d(TAG, "========================================")
    Log.d(TAG, "✅ MapComponent renderizado")
    Log.d(TAG, "========================================")
}
