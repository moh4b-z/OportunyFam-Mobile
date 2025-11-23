package com.oportunyfam_mobile.Components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.Usuario
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext

@Composable
fun PerfilPhoto(
    usuario: Usuario?,
    crianca: Crianca?,
    isUploading: Boolean
) {
    val fotoUrl = if (crianca != null) crianca.foto_perfil else usuario?.foto_perfil

    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Card(shape = CircleShape, elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), modifier = Modifier.size(120.dp)) {
            if (isUploading) {
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFFA000))
                }
            } else if (!fotoUrl.isNullOrEmpty()) {
                val ctx = LocalContext.current
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(ctx)
                        .data(fotoUrl)
                        .crossfade(true)
                        .build()
                )

                // Observe painter state for logging and provide a small fallback UI
                when (val state = painter.state) {
                    is coil.compose.AsyncImagePainter.State.Success -> {
                        Log.d("PerfilPhoto", "Imagem carregada com sucesso: $fotoUrl")
                    }
                    is coil.compose.AsyncImagePainter.State.Error -> {
                        Log.e("PerfilPhoto", "Falha ao carregar imagem: $fotoUrl -> ${state.result.throwable?.message}")
                    }
                    is coil.compose.AsyncImagePainter.State.Loading -> {
                        Log.d("PerfilPhoto", "Imagem carregando: $fotoUrl")
                    }
                    else -> { /* empty */ }
                }

                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )

             } else {
                 // placeholder icon centered
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                     Icon(Icons.Default.Person, contentDescription = "Sem foto")
                 }
             }
         }
     }
 }
