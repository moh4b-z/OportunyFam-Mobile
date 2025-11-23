package com.oportunyfam_mobile.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.ViewModel.PublicacoesState


// ============================================
// COMPONENTES DE PUBLICAÇÕES
// ============================================

@Composable
fun PublicacoesGrid(
    publicacoesState: PublicacoesState,
    instituicaoIdLogada: Int? = null,
    onDeletePublicacao: (Int) -> Unit = {},
    onEditPublicacao: (com.oportunyfam_mobile.model.Publicacao) -> Unit = {}
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
                            instituicaoIdLogada = instituicaoIdLogada,
                            onDelete = { onDeletePublicacao(publicacao.id) },
                            onEdit = { onEditPublicacao(publicacao) }
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
    publicacao: com.oportunyfam_mobile.model.Publicacao,
    instituicaoIdLogada: Int? = null,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    var showDetailDialog by remember { mutableStateOf(false) }

    // Verificar se o usuário tem permissão para editar/deletar
    val podeEditar = instituicaoIdLogada != null && instituicaoIdLogada == publicacao.id_instituicao

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(220.dp)
            .clickable { showDetailDialog = true }, // Abre o dialog ao clicar
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
                        placeholder = painterResource(id = R.drawable.perfil),
                        error = painterResource(id = R.drawable.perfil)
                    )
                }

                // Descrição (preview)
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
        }
    }

    // Dialog com visualização detalhada (estilo Instagram)
    if (showDetailDialog) {
        PublicacaoDetalhadaDialog(
            publicacao = publicacao,
            podeEditar = podeEditar,
            onDismiss = { showDetailDialog = false },
            onEdit = {
                showDetailDialog = false
                onEdit()
            },
            onDelete = {
                showDetailDialog = false
                onDelete()
            }
        )
    }
}

// ============================================
// DIALOG DE VISUALIZAÇÃO DETALHADA (ESTILO INSTAGRAM)
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicacaoDetalhadaDialog(
    publicacao: com.oportunyfam_mobile.model.Publicacao,
    podeEditar: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { /* Impede que cliques aqui fechem o dialog */ }
            ) {
                // Header com botões de ação
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp)
                ) {
                    // Botão voltar
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }

                    // edição/destruição removidas — visualização somente leitura
                }

                // Imagem principal (centralizada)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (!publicacao.imagem.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(publicacao.imagem)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Publicação detalhada",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit,
                            placeholder = painterResource(id = R.drawable.perfil),
                            error = painterResource(id = R.drawable.perfil)
                        )
                    }
                }

                // Descrição na parte inferior
                if (!publicacao.descricao.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                "Descrição",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                publicacao.descricao,
                                fontSize = 15.sp,
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // delete dialog removed - read-only
}
