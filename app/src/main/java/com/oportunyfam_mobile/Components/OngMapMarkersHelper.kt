package com.oportunyfam_mobile.Components

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oportunyfam_mobile.model.OngMapMarker

/**
 * Exemplo de como usar os marcadores de ONG no mapa
 * Use isso dentro do GoogleMap composable
 */
@Composable
fun OngMapMarkers(
    ongs: List<OngMapMarker>,
    onMarkerClick: (OngMapMarker) -> Unit = {}
) {
    ongs.forEach { ong ->
        Marker(
            state = MarkerState(position = LatLng(ong.latitude, ong.longitude)),
            title = ong.nome,
            snippet = ong.descricao,
            onClick = {
                onMarkerClick(ong)
                true
            }
        )
    }
}

/**
 * Dados de exemplo de ONGs com suas categorias
 */
fun getExampleOngs(): List<OngMapMarker> {
    return listOf(
        OngMapMarker(
            id = 1,
            nome = "Centro de Jiu Jitsu São Paulo",
            latitude = -23.5505,
            longitude = -46.6333,
            categorias = listOf(1), // Jiu Jitsu
            descricao = "Centro especializado em Jiu Jitsu",
            endereco = "Rua A, 123",
            telefone = "(11) 1234-5678",
            email = "contato@jiujitsu.com"
        ),
        OngMapMarker(
            id = 2,
            nome = "Hub de Tecnologia SP",
            latitude = -23.5565,
            longitude = -46.6500,
            categorias = listOf(2), // T.I
            descricao = "Centro de capacitação em tecnologia",
            endereco = "Av. Paulista, 456",
            telefone = "(11) 9876-5432",
            email = "contato@hubti.com"
        ),
        OngMapMarker(
            id = 3,
            nome = "Centro Cultural Brasileiro",
            latitude = -23.5450,
            longitude = -46.6200,
            categorias = listOf(3), // Centro Cultural
            descricao = "Espaço para arte e cultura",
            endereco = "Rua B, 789",
            telefone = "(11) 5555-5555",
            email = "contato@centrocultural.com"
        ),
        OngMapMarker(
            id = 4,
            nome = "Biblioteca Pública Central",
            latitude = -23.5550,
            longitude = -46.6400,
            categorias = listOf(4), // Biblioteca
            descricao = "Biblioteca com acervo completo",
            endereco = "Rua C, 321",
            telefone = "(11) 3333-3333",
            email = "contato@biblioteca.com"
        ),
        OngMapMarker(
            id = 5,
            nome = "Academia de Jiu Jitsu e T.I",
            latitude = -23.5480,
            longitude = -46.6350,
            categorias = listOf(1, 2), // Jiu Jitsu e T.I
            descricao = "ONG que oferece ambos os serviços",
            endereco = "Rua D, 654",
            telefone = "(11) 2222-2222",
            email = "contato@academiajiujitsueti.com"
        )
    )
}

/**
 * Filtra ONGs de acordo com as categorias selecionadas
 */
fun filterOngsByCategories(
    ongs: List<OngMapMarker>,
    selectedCategories: List<Int>
): List<OngMapMarker> {
    return if (selectedCategories.isEmpty()) {
        emptyList()
    } else {
        ongs.filter { ong ->
            ong.categorias.any { it in selectedCategories }
        }
    }
}

