package com.example.oportunyfam.Components

// Adicione esta importação para o Contexto
import androidx.compose.ui.platform.LocalContext
// Adicione esta importação para o Lifecycle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

// Este novo composable renderiza o mapa do OSM
@Composable
fun MapViewOsm(
    modifier: Modifier = Modifier,
    initialLat: Double = -25.441111, // Exemplo: Curitiba
    initialLon: Double = -49.276667, // Exemplo: Curitiba
    initialZoom: Double = 10.0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Configura o osmdroid
    Configuration.getInstance().load(context, context.getSharedPreferences("osm", 0))

    // Usa AndroidView para integrar a View MapView ao Compose
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(initialZoom)
            controller.setCenter(GeoPoint(initialLat, initialLon))
        }
    }

    // Gerencia o ciclo de vida do MapView (importante para evitar memory leaks)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Opcional: para liberar recursos mais agressivamente
            // mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
            .fillMaxSize()
    )
}