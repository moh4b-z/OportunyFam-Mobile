package com.oportunyfam_mobile.Components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.oportunyfam_mobile.model.Instituicao
import com.google.android.gms.maps.model.BitmapDescriptorFactory

private const val TAG = "MapViewGoogle"

@Composable
fun MapViewGoogle(
    modifier: Modifier = Modifier,
    initialLat: Double,
    initialLon: Double,
    initialZoom: Float = 15f,
    markers: List<Instituicao> = emptyList()
) {
    Log.d(TAG, "🗺️ Renderizando MapViewGoogle")
    Log.d(TAG, "📍 Posição inicial: Lat $initialLat, Lng $initialLon, Zoom $initialZoom")
    Log.d(TAG, "📊 Marcadores: ${markers.size}")

    val position = LatLng(initialLat, initialLon)
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, initialZoom)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            compassEnabled = true
        ),
        onMapLoaded = {
            Log.d(TAG, "✅ MapViewGoogle carregado com sucesso")
        }
    ) {
        // Adicionar marcadores
        markers.forEach { instituicao ->
            val lat = instituicao.endereco?.latitude
            val lng = instituicao.endereco?.longitude

            if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                Log.d(TAG, "📍 Adicionando marcador: ${instituicao.nome} em ($lat, $lng)")

                Marker(
                    state = MarkerState(position = LatLng(lat, lng)),
                    title = instituicao.nome,
                    snippet = instituicao.endereco.logradouro ?: "",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            } else {
                Log.w(TAG, "⚠️ Instituição ${instituicao.nome} sem coordenadas válidas")
            }
        }
    }
}

