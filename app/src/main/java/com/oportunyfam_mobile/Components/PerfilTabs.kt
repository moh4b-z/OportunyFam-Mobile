package com.oportunyfam_mobile.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.UsuarioMini

@Composable
fun PerfilTabs(
    selectedTab: String,
    criancas: List<Crianca> = emptyList(),
    responsaveis: List<UsuarioMini> = emptyList(),
    onChildClick: (Crianca) -> Unit = {},
    onChildDelete: (Crianca) -> Unit = {},
    onResponsavelClick: (UsuarioMini) -> Unit = {}
) {
    when (selectedTab) {
        "Informações" -> {
            // nothing here; parent shows InformacoesTab
        }
        "Crianças" -> {
            if (criancas.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Text("Nenhuma criança cadastrada")
                }
            } else {
                // Render as vertical list so it composes well inside the parent LazyColumn
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    criancas.forEach { crianca ->
                        ChildCard(child = crianca, onClick = { onChildClick(crianca) }, onDelete = { onChildDelete(crianca) })
                    }
                }
            }
        }
        "Responsáveis" -> {
            if (responsaveis.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Text("Nenhum responsável cadastrado")
                }
            } else {
                // Render lista de responsáveis
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    responsaveis.forEach { responsavel ->
                        ResponsavelCard(responsavel = responsavel, onClick = { onResponsavelClick(responsavel) })
                    }
                }
            }
        }
    }
}

@Composable
fun ResponsavelCard(responsavel: UsuarioMini, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar do responsável
            Card(
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                if (!responsavel.foto_perfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(responsavel.foto_perfil)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de ${responsavel.nome}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFFD27A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = responsavel.nome.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informações do responsável
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = responsavel.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Responsável",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Ícone de visualizar
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Ver perfil",
                tint = Color(0xFFFFA000)
            )
        }
    }
}
