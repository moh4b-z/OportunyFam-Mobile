package com.oportunyfam_mobile.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile.model.Publicacao
import com.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile.ViewModel.PublicacoesState


// ============================================
// COMPONENTES DE PUBLICAÇÕES
// ============================================

@Composable
fun PublicacoesGrid(
    publicacoesState: PublicacoesState,
    onDeletePublicacao: (Int) -> Unit
) {
    when (publicacoesState) {
        is PublicacoesState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFA000))
            }
        }
        is PublicacoesState.Success -> {
            val publicacoes = publicacoesState.publicacoes

            if (publicacoes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nenhuma publicação ainda",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Clique no + para adicionar fotos",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(publicacoes) { publicacao ->
                        PublicacaoCard(
                            publicacao = publicacao,
                            onDelete = { onDeletePublicacao(publicacao.id) }
                        )
                    }
                }
            }
        }
        is PublicacoesState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    publicacoesState.message,
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PublicacaoCard(
    publicacao: Publicacao,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column {
                // Imagem
                if (!publicacao.imagem.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(publicacao.imagem)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Publicação",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.instituicao),
                        error = painterResource(id = R.drawable.instituicao)
                    )
                }

                // Descrição
                if (!publicacao.descricao.isNullOrEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            publicacao.descricao,
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            maxLines = 3,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Botão deletar
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Deletar",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Deletar Publicação") },
            text = { Text("Deseja realmente deletar esta publicação?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Deletar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CriarPublicacaoDialog(
    descricao: String,
    imagemSelecionada: Boolean,
    isLoading: Boolean,
    onDescricaoChange: (String) -> Unit,
    onSelecionarImagem: () -> Unit,
    onSalvar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Nova Publicação", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = onDescricaoChange,
                    label = { Text("Descrição * (mín. 30 caracteres)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    maxLines = 6,
                    minLines = 4,
                    placeholder = { Text("Descreva sua publicação em detalhes...") },
                    isError = descricao.isNotEmpty() && descricao.trim().length < 30,
                    supportingText = {
                        Text(
                            text = "${descricao.trim().length}/30",
                            color = if (descricao.trim().length >= 30) Color.Gray else Color.Red,
                            fontSize = 12.sp
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSelecionarImagem,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (imagemSelecionada) Color(0xFF4CAF50) else Color(0xFFFFA000)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (imagemSelecionada) "Imagem Selecionada ✓" else "Selecionar Imagem *")
                }
            }
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFFFA000)
                )
            } else {
                TextButton(
                    onClick = onSalvar,
                    enabled = descricao.trim().length >= 30 && imagemSelecionada
                ) {
                    Text("Publicar", color = Color(0xFFFFA000), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}