package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.oportunyfam_mobile.model.Instituicao

@Composable
fun MapViewGoogle(
    modifier: Modifier = Modifier,
    initialLat: Double = -25.441111,
    initialLon: Double = -49.276667,
    initialZoom: Float = 10f,
    markers: List<Instituicao> = emptyList()
) {
    val defaultLocation = LatLng(initialLat, initialLon)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, initialZoom)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL
        )
    ) {
        // Adiciona marcadores para cada instituição
        markers.forEach { instituicao ->
            instituicao.endereco?.let { endereco ->
                if (endereco.latitude != null && endereco.longitude != null) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(endereco.latitude, endereco.longitude)
                        ),
                        title = instituicao.nome,
                        snippet = instituicao.descricao ?: ""
                    )
                }
            }
        }
    }
}
