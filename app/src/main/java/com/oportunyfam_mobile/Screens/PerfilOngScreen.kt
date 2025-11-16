package com.oportunyfam_mobile.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import android.util.Log
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Components.MapViewGoogle
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.Instituicao
import kotlinx.coroutines.launch
import com.google.android.gms.maps.MapsInitializer


@Composable
fun PerfilOngScreen(navController: NavHostController?, instituicaoId: Int = 0) {

    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedOption by remember { mutableStateOf<String?>(null) }
    var instituicao by remember { mutableStateOf<Instituicao?>(null) }
    var isMapReady by remember { mutableStateOf(false) }

    // Inicializar Google Maps
    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context)
            isMapReady = true
            Log.d("PerfilOngScreen", "✅ Google Maps inicializado com sucesso")
        } catch (e: Exception) {
            Log.e("PerfilOngScreen", "❌ Erro ao inicializar Google Maps: ${e.message}")
        }
    }

    // Buscar dados da instituição quando o ID mudar
    LaunchedEffect(instituicaoId) {
        if (instituicaoId > 0) {
            scope.launch {
                try {
                    Log.d("PerfilOngScreen", "Buscando instituição com ID: $instituicaoId")
                    val response = RetrofitFactory().getInstituicaoService().buscarPorIdSuspend(instituicaoId)
                    if (response.isSuccessful) {
                        instituicao = response.body()?.instituicao
                        Log.d("PerfilOngScreen", "Instituição carregada: ${instituicao?.nome}")
                    } else {
                        Log.e("PerfilOngScreen", "Erro ao buscar instituição: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("PerfilOngScreen", "Erro ao buscar instituição", e)
                }
            }
        }
    }

    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),
            Color(0xFFFFD27A)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificações", tint = Color.Black)
            }
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black)
            }
        }

        // Conteúdo principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            // Card branco principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 65.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Localização
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "",
                            tint = Color.Gray,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = instituicao?.endereco?.let {
                                "${it.cidade}-${it.estado}"
                            } ?: "Localização não disponível",
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mapa mostrando a localização da instituição
                    if (isMapReady && instituicao != null) {
                        val lat = instituicao?.endereco?.latitude ?: -25.441111
                        val lng = instituicao?.endereco?.longitude ?: -49.276667

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            MapViewGoogle(
                                modifier = Modifier.fillMaxSize(),
                                initialLat = lat,
                                initialLon = lng,
                                initialZoom = 15f,
                                markers = listOf(instituicao!!)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Telefone (se disponível)
                    instituicao?.telefone?.let {
                        if (it.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "",
                                    tint = Color.Gray,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(it, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Card de opções
                    Column(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth()
                            .background(Color(0xFFFFD580), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val buttons = listOf("Sobre nós", "Faça parte", "Associados")
                            buttons.forEach { label ->
                                Button(
                                    onClick = { selectedOption = label },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFB74D)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Mostra o card de descrição apenas se uma opção foi selecionada
                        selectedOption?.let { option ->
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    when (option) {
                                        "Sobre nós" -> Text(
                                            text = instituicao?.descricao ?: "Descrição não disponível",
                                            color = Color.Black
                                        )
                                        "Faça parte" -> CheckboxOportunidade()
                                        "Associados" -> Text(
                                            "Informações sobre nossos associados e como se tornar um.",
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 120.dp)
                )
            }

            // Texto acima da foto
            Text(
                text = "${instituicao?.conversas?.size ?: 0}\nPERFIL",
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 5.dp),
                textAlign = TextAlign.Center
            )

            // Foto de perfil
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 60.dp)
                    .border(3.dp, Color.White, CircleShape),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.user),
                    contentDescription = "foto perfil",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        BarraTarefas(navController = navController)
    }
}

@Composable
// Simulação simplificada - quando a API estiver pronta, usar os modelos corretos
fun CheckboxOportunidade() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Funcionalidade em desenvolvimento",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}



@Preview(showSystemUi = true)
@Composable
fun PerfilOngScreenPreview() {
    PerfilOngScreen(navController = null)
}
