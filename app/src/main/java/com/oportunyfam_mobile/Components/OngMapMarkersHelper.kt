package com.oportunyfam_mobile.Components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.oportunyfam_mobile.model.OngMapMarker
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.content.ContextCompat
import com.oportunyfam_mobile.R
import android.util.TypedValue
import androidx.compose.ui.platform.LocalDensity

/**
 * Converte um drawable (vector ou bitmap) para BitmapDescriptor compatível com Google Maps.
 */
fun bitmapDescriptorFromVector(context: android.content.Context, @DrawableRes vectorResId: Int): BitmapDescriptor {
    val drawable: Drawable? = ContextCompat.getDrawable(context, vectorResId)
    if (drawable == null) return BitmapDescriptorFactory.defaultMarker()

    // use density to create a sensible pixel size
    val density = context.resources.displayMetrics.density
    val dpSize = 48 // desired size in dp
    val width = (dpSize * density).toInt().coerceAtLeast(48)
    val height = (dpSize * density).toInt().coerceAtLeast(48)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Renderiza uma lista de marcadores com diferenciação visual.
 * - ongs: lista de OngMapMarker
 * - onMarkerClick: callback que recebe o OngMapMarker clicado
 */
@Composable
fun OngMapMarkers(
    ongs: List<OngMapMarker>,
    onMarkerClick: (OngMapMarker) -> Unit = {}
) {
    val context = LocalContext.current

    // cache descriptors para evitar recriar bitmaps a cada recomposição
    // Use default markers with different hues to avoid runtime bitmap decoding issues
    val apiDescriptor = remember { BitmapDescriptorFactory.defaultMarker(40f) } // orange-ish
    val externalDescriptor = remember { BitmapDescriptorFactory.defaultMarker(200f) }

    ongs.forEach { ong ->
        val position = LatLng(ong.latitude, ong.longitude)

        val icon: BitmapDescriptor = if (!ong.isExternal) apiDescriptor else externalDescriptor

        // Chamar Marker diretamente (sem try/catch ao redor de composable invocations)
        Marker(
            state = MarkerState(position = position),
            title = ong.nome,
            snippet = ong.descricao,
            icon = icon,
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
            email = "contato@jiujitsu.com",
            isExternal = false
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
            email = "contato@hubti.com",
            isExternal = false
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
            email = "contato@centrocultural.com",
            isExternal = false
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
            email = "contato@biblioteca.com",
            isExternal = false
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
            email = "contato@academiajiujitsueti.com",
            isExternal = false
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
