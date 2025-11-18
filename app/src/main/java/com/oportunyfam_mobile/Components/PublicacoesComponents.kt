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

                    // Botões de edição/deleção (apenas se podeEditar)
                    if (podeEditar) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = onEdit) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Deletar",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
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

    // Dialog de confirmação de deleção
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Deletar Publicação") },
            text = { Text("Deseja realmente deletar esta publicação?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("Deletar", color = Color.Red, fontWeight = FontWeight.Bold)
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

@Composable
fun EditarPublicacaoDialog(
    publicacao: com.oportunyfam_mobile.model.Publicacao,
    descricao: String,
    isLoading: Boolean,
    onDescricaoChange: (String) -> Unit,
    onSalvar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Editar Publicação", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // Mostrar imagem atual
                if (!publicacao.imagem.isNullOrEmpty()) {
                    Text(
                        "Imagem atual:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(publicacao.imagem)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Imagem atual",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.perfil),
                            error = painterResource(id = R.drawable.perfil)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Nota: A imagem não pode ser alterada na edição",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
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
                    enabled = descricao.trim().length >= 30
                ) {
                    Text("Salvar", color = Color(0xFFFFA000), fontWeight = FontWeight.Bold)
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
